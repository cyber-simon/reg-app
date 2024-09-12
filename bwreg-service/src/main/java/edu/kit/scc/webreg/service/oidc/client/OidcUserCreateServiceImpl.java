/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.oidc.client;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import edu.kit.scc.regapp.oidc.tools.OidcTokenHelper;
import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.audit.UserCreateAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.OidcUserDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.UserRoleEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.UserEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.group.OidcGroupUpdater;
import edu.kit.scc.webreg.service.identity.IdentityCreater;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.service.impl.OidcUserUpdater;
import edu.kit.scc.webreg.session.HttpRequestContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class OidcUserCreateServiceImpl implements OidcUserCreateService {

	@Inject
	private Logger logger;
	
	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private OidcUserDao oidcUserDao;
	
	@Inject
	private OidcTokenHelper tokenHelper;
	
	@Inject
	private OidcUserUpdater userUpdater;
	
	@Inject
	private RoleDao roleDao;

	@Inject
	private OidcRpConfigurationDao rpConfigDao;
	
	@Inject
	private IdentityDao identityDao;

	@Inject
	private SerialDao serialDao;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private OidcGroupUpdater oidcGroupUpdater;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private HttpRequestContext requestContext;
	
	@Inject
	private IdentityCreater identityCreater;

	@Override
	@RetryTransaction
	public OidcUserEntity preCreateUser(Long rpConfigId,
			String locale, Map<String, List<Object>> attributeMap)
			throws UserUpdateException {
		
		OidcRpConfigurationEntity rpConfig = rpConfigDao.fetch(rpConfigId);
		
		if (rpConfig == null) {
			throw new UserUpdateException("No such issuer/replying party");
		}
		
		IDTokenClaimsSet claims = tokenHelper.claimsFromMap(attributeMap);
		if (claims == null) { 
			throw new UserUpdateException("ID claims are missing in session");
		}

		UserInfo userInfo = tokenHelper.userInfoFromMap(attributeMap);
		if (userInfo == null) { 
			throw new UserUpdateException("User info is missing in session");
		}

		logger.debug("User {} from {} is being preCreated", claims.getSubject().getValue(), rpConfig.getName());
		
		OidcUserEntity entity = oidcUserDao.createNew();
		entity.setSubjectId(claims.getSubject().getValue());
		entity.setIssuer(rpConfig);
    	entity.setRoles(new HashSet<UserRoleEntity>());
    	entity.setAttributeStore(new HashMap<String, String>());
    	entity.setGenericStore(new HashMap<String, String>());
    	entity.setLocale(locale);

		entity.setEmail(userInfo.getEmailAddress());
		entity.setGivenName(userInfo.getGivenName());
		entity.setSurName(userInfo.getFamilyName());
    	entity.setEppn(userInfo.getStringClaim("eduPersonPrincipalName"));

    	return entity;
	}	

	@Override
	@RetryTransaction
	public OidcUserEntity createAndLinkUser(IdentityEntity identity, OidcUserEntity user,
			Map<String, List<Object>> attributeMap, String executor)
			throws UserUpdateException {
		logger.debug("Creating and link user {} to identity {}", user.getSubjectId(), identity.getId());

		identity = identityDao.fetch(identity.getId());
		
		UserCreateAuditor auditor = new UserCreateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-OidcUserCreate-Audit");
		auditor.setDetail("Create and link OIDC user " + user.getSubjectId());
		
		createUserInternal(user, attributeMap, executor, auditor);
		user.setIdentity(identity);

		user = postCreateUserInternal(user, attributeMap, executor, auditor);
		auditor.logAction(user.getEppn(), "CREATE USER", null, null, AuditStatus.SUCCESS);
		
		auditor.finishAuditTrail();
		auditor.commitAuditTrail();
		
		UserEvent userEvent = new UserEvent(user);

		try {
			eventSubmitter.submit(userEvent, EventType.USER_CREATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		
		return user;
	}
	
	@Override
	@RetryTransaction
	public IdentityEntity preMatchIdentity(OidcUserEntity user, Map<String, List<Object>> attributeMap) {
		return identityCreater.preMatchIdentity(user, attributeMap);
	}

	@Override
	@RetryTransaction
	public OidcUserEntity createUser(OidcUserEntity user,
			Map<String, List<Object>> attributeMap, String executor)
			throws UserUpdateException {

		UserCreateAuditor auditor = new UserCreateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-OidcUserCreate-Audit");
		auditor.setDetail("Create OIDC user " + user.getSubjectId());
		
		createUserInternal(user, attributeMap, executor, auditor);

		IdentityEntity identity = identityCreater.preCreateIdentity();

		user.setIdentity(identity);

		user = postCreateUserInternal(user, attributeMap, executor, auditor);
    	identityCreater.postCreateIdentity(identity, user);
		if (appConfig.getConfigValue("create_missing_eppn_scope") != null) {
			if (user.getEppn() == null) {
				String scope = appConfig.getConfigValue("create_missing_eppn_scope");
				user.setEppn(user.getIdentity().getGeneratedLocalUsername() + "@" + scope);
			}
		}

		auditor.logAction(user.getEppn(), "CREATE USER", null, null, AuditStatus.SUCCESS);
		
		auditor.finishAuditTrail();
		auditor.commitAuditTrail();
		
		UserEvent userEvent = new UserEvent(user);

		try {
			eventSubmitter.submit(userEvent, EventType.USER_CREATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}		

		return user;
	}
	
	@Override
	@RetryTransaction
	public OidcUserEntity postCreateUser(OidcUserEntity user, Map<String, List<Object>> attributeMap, String executor)
			throws UserUpdateException {
		StringBuffer debugLog = new StringBuffer();
		user = oidcUserDao.fetch(user.getId());
		return userUpdater.updateUser(user, attributeMap, executor, null, debugLog, resolveLastLoginHost());
	}

	private void createUserInternal(OidcUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			UserCreateAuditor auditor)
			throws UserUpdateException {
		logger.debug("Creating user {}", user.getSubjectId());

		
    	userUpdater.updateUserFromAttribute(user, attributeMap, true, auditor);
		
    	/** 
    	 * if user has no uid number yet, generate one
    	 */
		if (user.getUidNumber() == null) {
			user.setUidNumber(serialDao.nextUidNumber().intValue());
			logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
		}

		Map<String, String> attributeStore = user.getAttributeStore();
		for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
			attributeStore.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
		}
	
		user.setLastLoginHost(resolveLastLoginHost());
		
		user.setLastUpdate(new Date());

    	if (! UserStatus.ACTIVE.equals(user.getUserStatus())) {
    		user.setUserStatus(UserStatus.ACTIVE);
    		user.setLastStatusChange(new Date());
    	}
	}
	
	private OidcUserEntity postCreateUserInternal(OidcUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			UserCreateAuditor auditor)
			throws UserUpdateException {
    	user = oidcUserDao.persist(user);

    	roleDao.addUserToRole(user, "User");

    	oidcGroupUpdater.updateGroupsForUser(user, attributeMap, auditor);

    	StringBuffer debugLog = new StringBuffer();
    	userUpdater.updateUserNew(user, attributeMap, executor, auditor, debugLog, resolveLastLoginHost());
    			
		auditor.setUser(user);
		auditor.auditUserCreate();
		
		return user;
	}
	
	private String resolveLastLoginHost() {
		String lastLoginHost = null;
		if (requestContext != null && requestContext.getHttpServletRequest() != null) {
			lastLoginHost = requestContext.getHttpServletRequest().getServerName();
		}
		return lastLoginHost;
	}	
}
