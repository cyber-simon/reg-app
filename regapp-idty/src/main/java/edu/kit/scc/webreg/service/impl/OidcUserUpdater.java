package edu.kit.scc.webreg.service.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;

import edu.kit.scc.regapp.oidc.tools.OidcOpMetadataSingletonBean;
import edu.kit.scc.regapp.oidc.tools.OidcTokenHelper;
import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingOidcAttributeEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.hook.HookManager;
import edu.kit.scc.webreg.hook.UserServiceHook;
import edu.kit.scc.webreg.service.attribute.IncomingAttributesHandler;
import edu.kit.scc.webreg.service.attribute.IncomingOidcAttributesHandler;
import edu.kit.scc.webreg.service.group.HomeOrgGroupUpdater;
import edu.kit.scc.webreg.service.group.OidcGroupUpdater;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OidcUserUpdater extends AbstractUserUpdater<OidcUserEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SerialDao serialDao;

	@Inject
	private HookManager hookManager;

	@Inject
	private OidcGroupUpdater oidcGroupUpdater;

	@Inject
	private OidcTokenHelper oidcTokenHelper;

	@Inject
	private OidcOpMetadataSingletonBean opMetadataBean;
	
	@Inject
	private IncomingOidcAttributesHandler incomingAttributeHandler;

	public OidcUserEntity updateUserFromOP(OidcUserEntity user, String executor, StringBuffer debugLog)
			throws UserUpdateException {
		return updateUserFromHomeOrg(user, null, executor, debugLog);
	}
	
	public OidcUserEntity updateUserFromHomeOrg(OidcUserEntity user, ServiceEntity service, String executor, StringBuffer debugLog)
			throws UserUpdateException {

		try {
			/**
			 * TODO Implement refresh here
			 */
			OidcRpConfigurationEntity rpConfig = user.getIssuer();

			if (user.getAttributeStore().get("refreshToken") == null) {
				updateFail(user);
				throw new UserUpdateException("refresh token is null");
			}
			
			RefreshToken token = new RefreshToken(user.getAttributeStore().get("refreshToken"));
			AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(token);

			ClientID clientID = new ClientID(user.getIssuer().getClientId());
			Secret clientSecret = new Secret(user.getIssuer().getSecret());
			ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

			TokenRequest tokenRequest = new TokenRequest(opMetadataBean.getTokenEndpointURI(user.getIssuer()),
					clientAuth, refreshTokenGrant);
			TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

			if (!tokenResponse.indicatesSuccess()) {
				TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
				ErrorObject error = errorResponse.getErrorObject();
				logger.info("Got error: code {}, desc {}, http-status {}, uri {}", error.getCode(),
						error.getDescription());
				updateFail(user);
			} else {
				OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
				logger.debug("response: {}", oidcTokenResponse.toJSONObject());

				JWT idToken = oidcTokenResponse.getOIDCTokens().getIDToken();
				IDTokenClaimsSet claims = null;

				if (idToken != null) {
					IDTokenValidator validator = new IDTokenValidator(new Issuer(rpConfig.getServiceUrl()),
							new ClientID(rpConfig.getClientId()), JWSAlgorithm.RS256,
							opMetadataBean.getJWKSetURI(rpConfig).toURL());

					try {
						claims = validator.validate(idToken, null);
						logger.debug("Got signed claims verified from {}: {}", claims.getIssuer(), claims.getSubject());
					} catch (BadJOSEException | JOSEException e) {
						throw new UserUpdateException("signature failed: " + e.getMessage());
					}
				}

				RefreshToken refreshToken = null;

				if (oidcTokenResponse.getOIDCTokens().getRefreshToken() != null) {

					refreshToken = oidcTokenResponse.getOIDCTokens().getRefreshToken();
					try {
						JWT refreshJwt = JWTParser.parse(refreshToken.getValue());
						// Well, what to do with this info? Check if refresh token is short or long
						// lived? <1 day?
						logger.info("refresh will expire at: {}", refreshJwt.getJWTClaimsSet().getExpirationTime());
					} catch (java.text.ParseException e) {
						logger.debug("Refresh token is no JWT");
					}
				} else {
					logger.info("Answer contains no new refresh token, keeping old one");
				}

				BearerAccessToken bearerAccessToken = oidcTokenResponse.getOIDCTokens().getBearerAccessToken();

				HTTPResponse httpResponse = new UserInfoRequest(opMetadataBean.getUserInfoEndpointURI(rpConfig),
						bearerAccessToken).toHTTPRequest().send();

				UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);

				if (!userInfoResponse.indicatesSuccess()) {
					throw new UserUpdateException("got userinfo error response: "
							+ userInfoResponse.toErrorResponse().getErrorObject().getDescription());
				}

				UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
				logger.info("userinfo {}, {}, {}", userInfo.getSubject(), userInfo.getPreferredUsername(),
						userInfo.getEmailAddress());

				logger.debug("Updating OIDC user {}", user.getSubjectId());

				user = updateUser(user, claims, userInfo, refreshToken, bearerAccessToken, "web-sso", debugLog, null);

			}
		} catch (IOException | ParseException e) {
			logger.warn("Exception!", e);
		}

		return user;
	}

	public OidcUserEntity updateUser(OidcUserEntity user, IDTokenClaimsSet claims, UserInfo userInfo,
			RefreshToken refreshToken, BearerAccessToken bat, String executor, ServiceEntity service,
			StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {

		Map<String, List<Object>> attributeMap = oidcTokenHelper.convertToAttributeMap(claims, userInfo, refreshToken,
				bat);

		if (service != null)
			return updateUser(user, attributeMap, executor, service, debugLog, lastLoginHost);
		else
			return updateUser(user, attributeMap, executor, debugLog, lastLoginHost);
	}

	public OidcUserEntity updateUser(OidcUserEntity user, IDTokenClaimsSet claims, UserInfo userInfo,
			RefreshToken refreshToken, BearerAccessToken bat, String executor, StringBuffer debugLog,
			String lastLoginHost) throws UserUpdateException {

		return updateUser(user, claims, userInfo, refreshToken, bat, executor, null, debugLog, lastLoginHost);
	}

	public boolean updateUserNew(OidcUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			Auditor auditor, StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {
		boolean changed = false;

		changed |= preUpdateUser(user, attributeMap, user.getIssuer().getGenericStore(), executor, null, debugLog);
		changed |= updateUserFromAttribute(user, attributeMap, auditor);
		changed |= postUpdateUser(user, attributeMap, user.getIssuer().getGenericStore(), executor, null, debugLog,
				lastLoginHost);

		return changed;
	}

	public boolean updateUserFromAttribute(OidcUserEntity user, Map<String, List<Object>> attributeMap,
			boolean withoutUidNumber, Auditor auditor) throws UserUpdateException {

		boolean changed = false;

		UserServiceHook completeOverrideHook = null;
		Set<UserServiceHook> activeHooks = new HashSet<UserServiceHook>();

		for (UserServiceHook hook : hookManager.getUserHooks()) {
			if (hook.isResponsible(user, attributeMap)) {

				hook.preUpdateUserFromAttribute(user, attributeMap, auditor);
				activeHooks.add(hook);

				if (hook.isCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}

		if (completeOverrideHook == null) {
			IDTokenClaimsSet claims = oidcTokenHelper.claimsFromMap(attributeMap);
			if (claims == null) {
				logger.info("No claims set for user {}", user.getId());
			}

			UserInfo userInfo = oidcTokenHelper.userInfoFromMap(attributeMap);
			if (userInfo == null) {
				throw new UserUpdateException("User info is missing in session");
			}

			changed |= compareAndChangeProperty(user, "email", userInfo.getEmailAddress(), auditor);
			changed |= compareAndChangeProperty(user, "eppn", userInfo.getStringClaim("eduPersonPrincipalName"),
					auditor);
			changed |= compareAndChangeProperty(user, "givenName", userInfo.getGivenName(), auditor);
			changed |= compareAndChangeProperty(user, "surName", userInfo.getFamilyName(), auditor);

			if ((!withoutUidNumber) && (user.getUidNumber() == null)) {
				user.setUidNumber(serialDao.nextUidNumber().intValue());
				logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
				auditor.logAction(user.getEppn(), "SET FIELD", "uidNumber", "" + user.getUidNumber(),
						AuditStatus.SUCCESS);
				changed = true;
			}
		} else {
			logger.info("Overriding standard User Update Mechanism! Activator: {}",
					completeOverrideHook.getClass().getName());
		}

		for (UserServiceHook hook : activeHooks) {
			hook.postUpdateUserFromAttribute(user, attributeMap, auditor);
		}

		return changed;
	}

	private boolean compareAndChangeProperty(UserEntity user, String property, String value, Auditor auditor) {
		String s = null;
		String action = null;

		try {
			Object actualValue = PropertyUtils.getProperty(user, property);

			if (actualValue != null && actualValue.equals(value)) {
				// Value didn't change, do nothing
				return false;
			}

			if (actualValue == null && value == null) {
				// Value stayed null
				return false;
			}

			if (actualValue == null) {
				s = "null";
				action = "SET FIELD";
			} else {
				s = actualValue.toString();
				action = "UPDATE FIELD";
			}

			s = s + " -> " + value;
			if (s.length() > 1017)
				s = s.substring(0, 1017) + "...";

			PropertyUtils.setProperty(user, property, value);

			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.SUCCESS);
		} catch (IllegalAccessException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (InvocationTargetException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (NoSuchMethodException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		}

		return true;
	}

	protected void updateFail(OidcUserEntity user) {
		user.setLastFailedUpdate(new Date());
		user.setScheduledUpdate(getNextScheduledUpdate());
	}

	@Override
	public OidcUserEntity expireUser(OidcUserEntity user) throws UserUpdateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HomeOrgGroupUpdater<OidcUserEntity> getGroupUpdater() {
		return oidcGroupUpdater;
	}

	@Override
	public Map<String, String> resolveHomeOrgGenericStore(OidcUserEntity user) {
		return user.getIssuer().getGenericStore();
	}

	@Override
	public IncomingAttributesHandler<IncomingOidcAttributeEntity> resolveIncomingAttributeHandler(OidcUserEntity user) {
		return incomingAttributeHandler;
	}
}
