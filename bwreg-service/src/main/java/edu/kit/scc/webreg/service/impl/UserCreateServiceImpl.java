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
package edu.kit.scc.webreg.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.UserCreateAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserRoleEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.UserEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.UserCreateService;
import edu.kit.scc.webreg.service.group.HomeOrgGroupUpdater;
import edu.kit.scc.webreg.service.identity.IdentityCreater;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.SamlIdentifier;
import edu.kit.scc.webreg.session.HttpRequestContext;

@Stateless
public class UserCreateServiceImpl implements UserCreateService {

	@Inject
	private Logger logger;
	
	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private SamlUserDao samlUserDao;
	
	@Inject
	private UserUpdater userUpdater;
	
	@Inject
	private HomeOrgGroupUpdater homeOrgGroupUpdater;

	@Inject
	private RoleDao roleDao;

	@Inject
	private IdentityCreater identityCreater;
	
	@Inject
	private SerialDao serialDao;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private Saml2AssertionService saml2AssertionService;
	
	@Inject
	private SamlIdpMetadataService idpMetadataService;
	
	@Inject
	private HttpRequestContext requestContext;

	@Override
	public SamlUserEntity preCreateUser(SamlIdpMetadataEntity idpEntity, SamlSpConfigurationEntity spConfigEntity, SamlIdentifier samlIdentifier,
			String locale, Map<String, List<Object>> attributeMap)
			throws UserUpdateException {
		
		logger.debug("User ({}, {}, {}, {}, {}) from {} is being preCreated", samlIdentifier.getPersistentId(), 
				samlIdentifier.getPairwiseId(), samlIdentifier.getSubjectId(), samlIdentifier.getAttributeSourcedIdName(),
				samlIdentifier.getAttributeSourcedId(), idpEntity.getEntityId());
		
		idpEntity = idpMetadataService.findById(idpEntity.getId());
		
		SamlUserEntity entity = samlUserDao.createNew();
		entity.setIdp(idpEntity);
    	entity.setPersistentSpId(spConfigEntity.getEntityId());
    	entity.setRoles(new HashSet<UserRoleEntity>());
    	entity.setAttributeStore(new HashMap<String, String>());
    	entity.setGenericStore(new HashMap<String, String>());
    	entity.setLocale(locale);

		entity.setEmail(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:0.9.2342.19200300.100.1.3"));
		entity.setEppn(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
		entity.setGivenName(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:2.5.4.42"));
		entity.setSurName(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:2.5.4.4"));

		saml2AssertionService.updateUserIdentifier(samlIdentifier, entity, spConfigEntity.getEntityId(), null);
		
    	return entity;
	}	
	
	@Override
	public SamlUserEntity createUser(SamlUserEntity user,
			Map<String, List<Object>> attributeMap, String executor, StringBuffer debugLog)
			throws UserUpdateException {
		logger.debug("Creating user {}", user.getEppn());

		UserCreateAuditor auditor = new UserCreateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-UserCreate-Audit");
		auditor.setDetail("Create user " + user.getEppn());

		String lastLoginHost = null;
		if (requestContext != null && requestContext.getHttpServletRequest() != null) {
			lastLoginHost = requestContext.getHttpServletRequest().getLocalName();
		}
		
    	userUpdater.updateUserNew(user, attributeMap, executor, auditor, debugLog, lastLoginHost);
		
    	/** 
    	 * if user has no uid number yet, generate one
    	 */
		if (user.getUidNumber() == null) {
			user.setUidNumber(serialDao.next("uid-number-serial").intValue());
			logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
		}

		Map<String, String> attributeStore = user.getAttributeStore();
		for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
			attributeStore.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
		}
		
		user.setLastUpdate(new Date());
		user.setScheduledUpdate(getNextScheduledUpdate());
		
    	if (! UserStatus.ACTIVE.equals(user.getUserStatus())) {
    		user.setUserStatus(UserStatus.ACTIVE);
    		user.setLastStatusChange(new Date());
    	}

		IdentityEntity identity = identityCreater.preCreateIdentity();

		user.setIdentity(identity);
    	user = samlUserDao.persist(user);

    	identityCreater.postCreateIdentity(identity, user);
		if (appConfig.getConfigValue("create_missing_eppn_scope") != null) {
			if (user.getEppn() == null) {
				String scope = appConfig.getConfigValue("create_missing_eppn_scope");
				user.setEppn(user.getIdentity().getGeneratedLocalUsername() + "@" + scope);
			}
		}

    	roleDao.addUserToRole(user, "User");

    	homeOrgGroupUpdater.updateGroupsForUser(user, attributeMap, auditor);
    			
		auditor.setUser(user);
		auditor.auditUserCreate();
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
