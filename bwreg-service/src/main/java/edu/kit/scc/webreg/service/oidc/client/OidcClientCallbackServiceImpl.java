package edu.kit.scc.webreg.service.oidc.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.MDC;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpFlowStateDao;
import edu.kit.scc.webreg.dao.oidc.OidcUserDao;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;

@Stateless
public class OidcClientCallbackServiceImpl implements OidcClientCallbackService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private OidcRpConfigurationDao rpConfigDao;
	
	@Inject
	private OidcRpFlowStateDao rpFlowStateDao;
	
	@Inject
	private OidcOpMetadataSingletonBean opMetadataBean;

	@Inject
	private OidcUserDao oidcUserDao;
	
	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private OidcTokenHelper oidcTokenHelper;
	
	@Inject
	private OidcUserUpdater userUpdater;
	
	@Inject
	private UserLoginInfoDao userLoginInfoDao;
	
	@Inject
	private SessionManager session;
		
	@Override
	public void callback(String uri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws OidcAuthenticationException {

		try {
			AuthorizationResponse response = AuthorizationResponse.parse(new URI(uri));
		
			if (! response.indicatesSuccess()) {
				throw new OidcAuthenticationException("No success indicated with uri parsing");
			}
	
			AuthorizationSuccessResponse successResponse = (AuthorizationSuccessResponse) response;
	
			OidcRpFlowStateEntity flowState = rpFlowStateDao.findByState(successResponse.getState().getValue());
			
			// The returned state parameter must match the one send with the request
			if (flowState == null) {
				throw new OidcAuthenticationException("State is wrong or expired");
			}

			OidcRpConfigurationEntity rpConfig = flowState.getRpConfiguration();
			
			AuthorizationCode code = successResponse.getAuthorizationCode();
			flowState.setCode(code.getValue());

			String callbackUrl;
			if (! rpConfig.getCallbackUrl().startsWith("https://")) {
				/*
				 * we are dealing with a relative acs endpoint. We have to build it with the called hostname;
				 */
				callbackUrl = "https://" + httpServletRequest.getServerName() + rpConfig.getCallbackUrl();
			}
			else {
				callbackUrl = rpConfig.getCallbackUrl();
			}

			URI callback = new URI(callbackUrl);
			AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);

			ClientID clientID = new ClientID(rpConfig.getClientId());
			Secret clientSecret = new Secret(rpConfig.getSecret());
			ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

			// Make the token request
			TokenRequest tokenRequest = new TokenRequest(opMetadataBean.getTokenEndpointURI(rpConfig), 
					clientAuth, codeGrant);
			TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

			if (! tokenResponse.indicatesSuccess()) {
			    throw new OidcAuthenticationException("got token error response: " + tokenResponse.toErrorResponse().getErrorObject().getDescription());
			}

			OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();

			JWT idToken = oidcTokenResponse.getOIDCTokens().getIDToken();

			IDTokenValidator validator = new IDTokenValidator(
					new Issuer(rpConfig.getServiceUrl()), 
					new ClientID(rpConfig.getClientId()), 
					JWSAlgorithm.RS256, 
					opMetadataBean.getJWKSetURI(rpConfig).toURL());

			IDTokenClaimsSet claims;
			
			try {
				claims = validator.validate(idToken, new Nonce(flowState.getNonce()));
				logger.debug("Got signed claims verified from {}: {}", claims.getIssuer(), claims.getSubject());
			} catch (BadJOSEException | JOSEException e) {
			    throw new OidcAuthenticationException("signature failed: " + e.getMessage());
			} 
					
			AccessToken accessToken = oidcTokenResponse.getOIDCTokens().getAccessToken();
			RefreshToken refreshToken = oidcTokenResponse.getOIDCTokens().getRefreshToken();

			BearerAccessToken bat = oidcTokenResponse.getOIDCTokens().getBearerAccessToken();
			HTTPResponse httpResponse = new UserInfoRequest(
					opMetadataBean.getUserInfoEndpointURI(rpConfig), bat)
				    .toHTTPRequest()
				    .send();
			
			UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);

			if (! userInfoResponse.indicatesSuccess()) {
			    throw new OidcAuthenticationException("got userinfo error response: " + 
			    		userInfoResponse.toErrorResponse().getErrorObject().getDescription());
			}
			
			UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
			logger.info("userinfo {}, {}, {}", userInfo.getSubject(), userInfo.getPreferredUsername(), 
					userInfo.getEmailAddress());
			
			OidcUserEntity user = oidcUserDao.findByIssuerAndSub(rpConfig, claims.getSubject().getValue());

			if (user != null) {
				MDC.put("userId", "" + user.getId());
			}

			if (user == null) {
				logger.info("New User detected, sending to register Page");

				// Store OIDC Data temporarily in Session
				logger.debug("Storing relevant Oidc data in session");
				session.setSubjectId(claims.getSubject().getValue());
				session.setAttributeMap(oidcTokenHelper.convertToAttributeMap(claims, userInfo));
				
				httpServletResponse.sendRedirect("/register/register-oidc.xhtml");
				return;
			}
			
	    	logger.debug("Updating OIDC user {}", user.getSubjectId());
			
			try {
				user = userUpdater.updateUser(user, claims, userInfo, "web-sso");
			} catch (UserUpdateException e) {
				logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
				throw new OidcAuthenticationException(e.getMessage());
			}
			
			session.setIdentityId(user.getIdentity().getId());
			session.setLoginTime(Instant.now());
			session.setTheme(user.getTheme());
			session.setLocale(user.getLocale());
			
			UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
			loginInfo.setUser(user);
			loginInfo.setLoginDate(new Date());
			loginInfo.setLoginMethod(UserLoginMethod.HOME_ORG);
			loginInfo.setLoginStatus(UserLoginInfoStatus.SUCCESS);
			loginInfo.setFrom(httpServletRequest.getRemoteAddr());
			loginInfo = userLoginInfoDao.persist(loginInfo);
			
			if (session.getOriginalRequestPath() != null) {
				String orig = session.getOriginalRequestPath();
				session.setOriginalRequestPath(null);
				httpServletResponse.sendRedirect(orig);
			}
			else
				httpServletResponse.sendRedirect("/index.xhtml");

			return;
			
		} catch (IOException | ParseException | URISyntaxException e) {
			logger.warn("Oidc callback failed: {}", e.getMessage());
			throw new OidcAuthenticationException(e);
		}
	}
}