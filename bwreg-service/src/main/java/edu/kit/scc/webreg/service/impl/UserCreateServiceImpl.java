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

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.UserCreateAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserRoleEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.UserEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SerialService;
import edu.kit.scc.webreg.service.UserCreateService;

@Stateless
public class UserCreateServiceImpl implements UserCreateService {

	@Inject
	private Logger logger;
	
	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private UserDao userDao;
	
	@Inject
	private UserUpdater userUpdater;
	
	@Inject
	private HomeOrgGroupUpdater homeOrgGroupUpdater;

	@Inject
	private RoleDao roleDao;

	@Inject
	private SerialService serialService;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private ApplicationConfig appConfig;
	
	@Override
	public UserEntity preCreateUser(SamlIdpMetadataEntity idpEntity, SamlSpConfigurationEntity spConfigEntity, String persistentId,
			String locale, Map<String, List<Object>> attributeMap)
			throws UserUpdateException {
		
		logger.debug("User {} from {} is being preCreated", persistentId, idpEntity.getEntityId());
		
		UserEntity entity = userDao.createNew();
		entity.setIdp(idpEntity);
    	entity.setPersistentIdpId(idpEntity.getEntityId());
    	entity.setPersistentSpId(spConfigEntity.getEntityId());
    	entity.setPersistentId(persistentId);
    	entity.setRoles(new HashSet<UserRoleEntity>());
    	entity.setAttributeStore(new HashMap<String, String>());
    	entity.setGenericStore(new HashMap<String, String>());
    	entity.setLocale(locale);

		entity.setEmail(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:0.9.2342.19200300.100.1.3"));
		entity.setEppn(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
		entity.setGivenName(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:2.5.4.42"));
		entity.setSurName(attrHelper.getSingleStringFirst(attributeMap, "urn:oid:2.5.4.4"));
    	
    	return entity;
	}	
	
	@Override
	public UserEntity createUser(UserEntity user,
			Map<String, List<Object>> attributeMap, String executor)
			throws UserUpdateException {
		logger.debug("Creating user {}", user.getEppn());

		UserCreateAuditor auditor = new UserCreateAuditor(auditDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor);
		auditor.setName(getClass().getName() + "-UserCreate-Audit");
		auditor.setDetail("Create user " + user.getEppn());
		
    	userUpdater.updateUserFromAttribute(user, attributeMap, true, auditor);
		
    	/** 
    	 * if user has no uid number yet, generate one
    	 */
		if (user.getUidNumber() == null) {
			user.setUidNumber(serialService.next("uid-number-serial").intValue());
			logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
		}

		Map<String, String> attributeStore = user.getAttributeStore();
		for (Entry<String, List<Object>> entry : attributeMap.entrySet()) {
			attributeStore.put(entry.getKey(), attrHelper.attributeListToString(entry.getValue()));
		}
		
		user.setLastUpdate(new Date());

    	if (! UserStatus.ACTIVE.equals(user.getUserStatus())) {
    		user.setUserStatus(UserStatus.ACTIVE);
    		user.setLastStatusChange(new Date());
    	}

    	user = userDao.persist(user);

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
}
