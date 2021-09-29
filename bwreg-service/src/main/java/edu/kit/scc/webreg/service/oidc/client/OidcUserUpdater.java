package edu.kit.scc.webreg.service.oidc.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

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

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.audit.RegistryAuditor;
import edu.kit.scc.webreg.audit.UserUpdateAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.as.ASUserAttrDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.dao.oidc.OidcUserDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.UserEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.hook.UserServiceHook;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.impl.AbstractUserUpdater;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.service.impl.HookManager;
import edu.kit.scc.webreg.service.reg.AttributeSourceQueryService;
import edu.kit.scc.webreg.service.reg.impl.Registrator;

@ApplicationScoped
public class OidcUserUpdater extends AbstractUserUpdater<OidcUserEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private OidcUserDao userDao;

	@Inject
	private ServiceService serviceService;
	
	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private SerialService serialService;
	
	@Inject
	private HookManager hookManager;
	
	@Inject
	private OidcGroupUpdater oidcGroupUpdater;
	
	@Inject
	private ASUserAttrDao asUserAttrDao;
	
	@Inject
	private AttributeSourceQueryService attributeSourceQueryService;
	
	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private Registrator registrator;
	
	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private OidcTokenHelper oidcTokenHelper;
	
	@Inject
	private OidcOpMetadataSingletonBean opMetadataBean;

	public OidcUserEntity updateUserFromOP(OidcUserEntity user, String executor, StringBuffer debugLog) 
			throws UserUpdateException {
		user = userDao.merge(user);

		try {
			/**
			 * TODO Implement refresh here
			 */
			OidcRpConfigurationEntity rpConfig = user.getIssuer();
			
			RefreshToken token = new RefreshToken(user.getAttributeStore().get("refreshToken"));
			AuthorizationGrant refreshTokenGrant = new RefreshTokenGrant(token);

			ClientID clientID = new ClientID(user.getIssuer().getClientId());
			Secret clientSecret = new Secret(user.getIssuer().getSecret());
			ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

			TokenRequest tokenRequest = new TokenRequest(opMetadataBean.getTokenEndpointURI(user.getIssuer()), 
							clientAuth, refreshTokenGrant);
			TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

			if (! tokenResponse.indicatesSuccess()) {
				TokenErrorResponse errorResponse = tokenResponse.toErrorResponse();
				ErrorObject error = errorResponse.getErrorObject();
				logger.info("Got error: code {}, desc {}, http-status {}, uri {}", error.getCode(), error.getDescription());
			}
			else {
				OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
				logger.debug("response: {}", oidcTokenResponse.toJSONObject());

				JWT idToken = oidcTokenResponse.getOIDCTokens().getIDToken();
				IDTokenClaimsSet claims = null;

				if (idToken != null) {
					IDTokenValidator validator = new IDTokenValidator(
							new Issuer(rpConfig.getServiceUrl()), 
							new ClientID(rpConfig.getClientId()), 
							JWSAlgorithm.RS256, 
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
						// Well, what to do with this info? Check if refresh token is short or long lived? <1 day?
						logger.info("refresh will expire at: {}", refreshJwt.getJWTClaimsSet().getExpirationTime());
					} catch (java.text.ParseException e) {
						logger.debug("Refresh token is no JWT");
					}
				}
				else {
					logger.info("Answer contains no new refresh token, keeping old one");
				}
				
				BearerAccessToken bearerAccessToken = oidcTokenResponse.getOIDCTokens().getBearerAccessToken();

				HTTPResponse httpResponse = new UserInfoRequest(
						opMetadataBean.getUserInfoEndpointURI(rpConfig), bearerAccessToken)
					    .toHTTPRequest()
					    .send();
				
				UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);

				if (! userInfoResponse.indicatesSuccess()) {
				    throw new UserUpdateException("got userinfo error response: " + 
				    		userInfoResponse.toErrorResponse().getErrorObject().getDescription());
				}
				
				UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
				logger.info("userinfo {}, {}, {}", userInfo.getSubject(), userInfo.getPreferredUsername(), 
						userInfo.getEmailAddress());

		    	logger.debug("Updating OIDC user {}", user.getSubjectId());
				
				user = updateUser(user, claims, userInfo, refreshToken, bearerAccessToken, "web-sso", debugLog);

			}
		}
		catch (IOException | ParseException e) {
			logger.warn("Exception!", e);
		}
		
		return user;
	}
	
	public OidcUserEntity updateUser(OidcUserEntity user, Map<String, List<Object>> attributeMap, String executor, StringBuffer debugLog)
			throws UserUpdateException {
		return updateUser(user, attributeMap, executor, null, null);
	}
	
	public OidcUserEntity updateUser(OidcUserEntity user, Map<String, List<Object>> attributeMap, String executor, 
			ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException {
		MDC.put("userId", "" + user.getId());
		logger.debug("Updating OIDC user {}", user.getEppn());

		user = userDao.merge(user);
		
		boolean changed = false;
		
		UserUpdateAuditor auditor = new UserUpdateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-UserUpdate-Audit");
		auditor.setDetail("Update OIDC user " + user.getSubjectId());

		changed |= preUpdateUser(user, attributeMap, user.getIssuer().getGenericStore(), executor, service, debugLog);
		
		// List to store parent services, that are not registered. Need to be registered
		// later, when attribute map is populated
		List<ServiceEntity> delayedRegisterList = new ArrayList<ServiceEntity>();
		
		/**
		 * put no_assertion_count in generic store if assertion is missing. Else
		 * reset no assertion count and put last valid assertion date in
		 */
		if (attributeMap == null) {
			if (! user.getGenericStore().containsKey("no_assertion_count")) {
				user.getGenericStore().put("no_assertion_count", "1");
			}
			else {
				user.getGenericStore().put("no_assertion_count", 
						"" + (Long.parseLong(user.getGenericStore().get("no_assertion_count")) + 1L));
			}
			
			logger.info("No attribute for user {}, skipping updateFromAttribute", user.getEppn());
			
			user.getAttributeStore().clear();

			if (UserStatus.ACTIVE.equals(user.getUserStatus())) {
				changeUserStatus(user, UserStatus.ON_HOLD, auditor);

				/*
				 * Also flag all registries for user ON_HOLD
				 */
				List<RegistryEntity> registryList = registryDao.findByUserAndStatus(user, 
						RegistryStatus.ACTIVE, RegistryStatus.LOST_ACCESS, RegistryStatus.INVALID);
				for (RegistryEntity registry : registryList) {
					changeRegistryStatus(registry, RegistryStatus.ON_HOLD, "user-on-hold", auditor);
				}
			}
		}
		else {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			user.getGenericStore().put("no_assertion_count", "0");
			user.getGenericStore().put("last_valid_assertion", df.format(new Date()));
		
			changed |= updateUserFromAttribute(user, attributeMap, auditor);
			
			if (UserStatus.ON_HOLD.equals(user.getUserStatus())) {
				changeUserStatus(user, UserStatus.ACTIVE, auditor);
				
				/*
				 * Also reenable all registries for user to LOST_ACCESS. 
				 * They are rechecked then
				 */
				List<RegistryEntity> registryList = registryDao.findByUserAndStatus(user, 
						RegistryStatus.ON_HOLD);
				for (RegistryEntity registry : registryList) {
					changeRegistryStatus(registry, RegistryStatus.LOST_ACCESS, "user-reactivated", auditor);
					
					/*
					 * check if parent registry is missing
					 */
					if (registry.getService().getParentService() != null) {
						List<RegistryEntity> parentRegistryList = registryDao.findByServiceAndIdentityAndNotStatus(
								registry.getService().getParentService(), user.getIdentity(), 
								RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
						if (parentRegistryList.size() == 0) {
							delayedRegisterList.add(registry.getService().getParentService());
						}
					}
				}
				
				/*
				 * fire a user changed event to be sure, when the user is activated
				 */
				changed = true;
			}

			/*
			 * if service is set, update only attribute sources spcific for this 
			 * service. Else update all (login via web or generic attribute query)
			 */
			if (service != null) {
				service = serviceService.findByIdWithAttrs(service.getId(), "attributeSourceService");
				
				for (AttributeSourceServiceEntity asse : service.getAttributeSourceService()) {
					changed |= attributeSourceQueryService.updateUserAttributes(user, asse.getAttributeSource(), executor);
				}
			}
			else {
				List<ASUserAttrEntity> asUserAttrList = asUserAttrDao.findForUser(user);
				for (ASUserAttrEntity asUserAttr : asUserAttrList) {
					changed |= attributeSourceQueryService.updateUserAttributes(user, asUserAttr.getAttributeSource(), executor);
				}
			}
			
			Set<GroupEntity> changedGroups = oidcGroupUpdater.updateGroupsForUser(user, attributeMap, auditor);

			if (changedGroups.size() > 0) {
				changed = true;
			}

			Map<String, String> attributeStore = user.getAttributeStore();
			for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
				attributeStore.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
			}
		}

		for (ServiceEntity delayedService : delayedRegisterList) {
			try {
				registrator.registerUser(user, delayedService, 
						"user-" + user.getId(), false);
			} catch (RegisterException e) {
				logger.warn("Parent registrytion didn't work out like it should", e);
			}
		}

		changed |= postUpdateUser(user, attributeMap, user.getIssuer().getGenericStore(), executor, service, debugLog);
		
		user.setLastUpdate(new Date());
		user.setLastFailedUpdate(null);
		user.setScheduledUpdate(getNextScheduledUpdate());
		
		if (changed) {
			fireUserChangeEvent(user, auditor.getActualExecutor(), auditor);
		}
		
		auditor.setUser(user);
		auditor.finishAuditTrail();
		auditor.commitAuditTrail();
		
		return user;
	}
	
	public OidcUserEntity updateUser(OidcUserEntity user, IDTokenClaimsSet claims, UserInfo userInfo, 
			RefreshToken refreshToken, BearerAccessToken bat, String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException {

		Map<String, List<Object>> attributeMap = oidcTokenHelper.convertToAttributeMap(claims, userInfo, refreshToken, bat);

		if (service != null)
			return updateUser(user, attributeMap, executor, service, debugLog);
		else
			return updateUser(user, attributeMap, executor, debugLog);
	}

	public OidcUserEntity updateUser(OidcUserEntity user, IDTokenClaimsSet claims, UserInfo userInfo, 
			RefreshToken refreshToken, BearerAccessToken bat, String executor, StringBuffer debugLog)
			throws UserUpdateException {
		
		return updateUser(user, claims, userInfo, refreshToken, bat, executor, null, debugLog);
	}
	
	protected void fireUserChangeEvent(UserEntity user, String executor, Auditor auditor) {
		
		UserEvent userEvent = new UserEvent(user, auditor.getAudit());
		
		try {
			eventSubmitter.submit(userEvent, EventType.USER_UPDATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}

	public boolean updateUserFromAttribute(UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor) 
				throws UserUpdateException {
		return updateUserFromAttribute(user, attributeMap, false, auditor);
	}

	public boolean updateUserFromAttribute(UserEntity user, Map<String, List<Object>> attributeMap, boolean withoutUidNumber, Auditor auditor) 
				throws UserUpdateException {

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
			changed |= compareAndChangeProperty(user, "eppn", userInfo.getStringClaim("eduPersonPrincipalName"), auditor);
			changed |= compareAndChangeProperty(user, "givenName", userInfo.getGivenName(), auditor);
			changed |= compareAndChangeProperty(user, "surName", userInfo.getFamilyName(), auditor);

			if ((! withoutUidNumber) && (user.getUidNumber() == null)) {
				user.setUidNumber(serialService.next("uid-number-serial").intValue());
				logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
				auditor.logAction(user.getEppn(), "SET FIELD", "uidNumber", "" + user.getUidNumber(), AuditStatus.SUCCESS);
				changed = true;
			}
		}
		else {
			logger.info("Overriding standard User Update Mechanism! Activator: {}", completeOverrideHook.getClass().getName());
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
			}
			else {
				s = actualValue.toString();
				action = "UPDATE FIELD";
			}
			
			s = s + " -> " + value;
			if (s.length() > 1017) s = s.substring(0, 1017) + "...";
			
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
	
	protected void changeUserStatus(UserEntity user, UserStatus toStatus, Auditor auditor) {
		UserStatus fromStatus = user.getUserStatus();
		user.setUserStatus(toStatus);
		user.setLastStatusChange(new Date());
		
		logger.debug("{}: change user status from {} to {}", user.getEppn(), fromStatus, toStatus);
		auditor.logAction(user.getEppn(), "CHANGE STATUS", fromStatus + " -> " + toStatus, 
				"Change status " + fromStatus + " -> " + toStatus, AuditStatus.SUCCESS);
	}
	
	protected void changeRegistryStatus(RegistryEntity registry, RegistryStatus toStatus, String statusMessage, Auditor parentAuditor) {
		RegistryStatus fromStatus = registry.getRegistryStatus();
		registry.setRegistryStatus(toStatus);
		registry.setStatusMessage(statusMessage);
		registry.setLastStatusChange(new Date());

		logger.debug("{} {} {}: change registry status from {} to {}", new Object[] { 
				registry.getUser().getEppn(), registry.getService().getShortName(), registry.getId(), fromStatus, toStatus });
		RegistryAuditor registryAuditor = new RegistryAuditor(auditDao, auditDetailDao, appConfig);
		registryAuditor.setParent(parentAuditor);
		registryAuditor.startAuditTrail(parentAuditor.getActualExecutor());
		registryAuditor.setName(getClass().getName() + "-UserUpdate-Registry-Audit");
		registryAuditor.setDetail("Update registry " + registry.getId() + " for user " + registry.getUser().getEppn());
		registryAuditor.setRegistry(registry);
		registryAuditor.logAction(registry.getUser().getEppn(), "CHANGE STATUS", "registry-" + registry.getId(), 
				"Change status " + fromStatus + " -> " + toStatus, AuditStatus.SUCCESS);
		registryAuditor.finishAuditTrail();
	}
	
	private Date getNextScheduledUpdate() {
		Long futureMillis = 30L * 24L * 60L * 60L * 1000L;
		if (appConfig.getConfigOptions().containsKey("update_schedule_future")) {
			futureMillis = Long.decode(appConfig.getConfigValue("update_schedule_future"));
		}
		Integer futureMillisRandom = 6 * 60 * 60 * 1000;
		if (appConfig.getConfigOptions().containsKey("update_schedule_future_random")) {
			futureMillisRandom = Integer.decode(appConfig.getConfigValue("update_schedule_future_random"));
		}
		Random r = new Random();
		return new Date(System.currentTimeMillis() + futureMillis + r.nextInt(futureMillisRandom));
	}	
}
