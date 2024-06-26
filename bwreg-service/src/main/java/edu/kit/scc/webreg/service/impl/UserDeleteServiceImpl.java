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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.UserDeleteAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.SamlAssertionDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.ServiceEventDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.jpa.attribute.IncomingAttributeSetDao;
import edu.kit.scc.webreg.dao.jpa.attribute.ValueDao;
import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingAttributeSetEntity_;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthUserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectGroupEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.GenericMapDataEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.UserDeleteService;
import edu.kit.scc.webreg.service.reg.impl.Approvor;
import edu.kit.scc.webreg.service.reg.impl.Registrator;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class UserDeleteServiceImpl implements UserDeleteService {

	@Inject
	private Logger logger;

	@Inject
	private Registrator registrator;

	@Inject
	private Approvor approvor;

	@Inject
	private AuditEntryDao auditEntryDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private IdentityDao identityDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private GroupDao groupDao;

	@Inject
	private RoleDao roleDao;

	@Inject
	private SerialDao serialDao;

	@Inject
	private UserDao userDao;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private SamlAssertionDao samlAssertionDao;

	@Inject
	private ServiceEventDao serviceEventDao;

	@Inject
	private IncomingAttributeSetDao incomingAttributeSetDao;

	@Inject
	private ValueDao valueDao;

	@Inject
	private EventSubmitter eventSubmitter;

	@Override
	public void unlinkAndDeleteAccount(UserEntity user, String executor) {
		user = userDao.fetch(user.getId());
		IdentityEntity identity = user.getIdentity();
		logger.info("Unlink and delete user account {} from identity {}", user.getId(), identity.getId());

		UserDeleteAuditor auditor = new UserDeleteAuditor(auditEntryDao, auditDetailDao, appConfig);
		auditor.startAuditTrail(executor, true);
		auditor.setName("UnlinkAndDeleteUser-Audit");
		auditor.setDetail("unlink and delete user " + user.getEppn() + " " + user.getId());
		auditor.setUser(user);

		deleteAllForUser(user, executor, auditor);
		user.setIdentity(null);

		auditor.finishAuditTrail();
		auditor.commitAuditTrail();

	}

	@Override
	public void deleteUserData(IdentityEntity identity, String executor) {
		logger.info("Delete all personal user data for identity {}", identity.getId());
		identity = identityDao.fetch(identity.getId());

		List<UserEntity> userList = userDao.findByIdentity(identity);

		for (UserEntity user : userList) {
			logger.info("Delete all personal user data for user {}", user.getId());

			UserDeleteAuditor auditor = new UserDeleteAuditor(auditEntryDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor, true);
			auditor.setName("UserDelete-Audit");
			auditor.setDetail("delete all personal data for user " + user.getEppn() + " " + user.getId());
			auditor.setUser(user);

			deleteAllForUser(user, executor, auditor);

			auditor.finishAuditTrail();
			auditor.commitAuditTrail();
		}

		identity.setTwoFaUserId("deleted-" + UUID.randomUUID().toString());
		identity.setTwoFaUserName("deleted-" + UUID.randomUUID().toString());
		identity.setUidNumber(serialDao.nextUidNumber().intValue());
	}

	private void deleteAllForUser(UserEntity user, String executor, UserDeleteAuditor auditor) {
		deleteAllRegistriesForUser(user, executor, auditor);
		deleteAllGroupsForUser(user, executor, auditor);
		deleteAllValuesForUser(user, executor, auditor);

		user.getGenericStore().clear();
		user.getAttributeStore().clear();
		user.getUserAttrs().clear();
		user.getEmailAddresses().clear();
		user.setEmail(null);
		user.setEppn(null);
		user.setGivenName(null);
		user.setSurName(null);
		if (user instanceof SamlUserEntity) {
			((SamlUserEntity) user).setPersistentId(null);
			((SamlUserEntity) user).setAttributeSourcedId(null);
			((SamlUserEntity) user).setAttributeSourcedIdName(null);
			((SamlUserEntity) user).setSubjectId(null);
			samlAssertionDao.deleteAssertionForUser((SamlUserEntity) user);
		}
		else if (user instanceof OidcUserEntity) {
			((OidcUserEntity) user).setSubjectId(null);
			((OidcUserEntity) user).setIssuer(null);
		}
		else if (user instanceof OAuthUserEntity) {
			((OAuthUserEntity) user).setOauthId(null);
			((OAuthUserEntity) user).setOauthIssuer(null);
		}
		user.setUidNumber(serialDao.nextUidNumber().intValue());
		user.setUserStatus(UserStatus.DEREGISTERED);
		user.setLastStatusChange(new Date());

		deleteAllRolesForUser(user, executor, auditor);
	}

	private void deleteAllValuesForUser(UserEntity user, String executor, UserDeleteAuditor auditor) {

		List<IncomingAttributeSetEntity> attributeSetList = incomingAttributeSetDao.findAll(equal(IncomingAttributeSetEntity_.user, user));
		
		for (IncomingAttributeSetEntity attributeSet : attributeSetList) {
			List<ValueEntity> valueList = valueDao.findAll(equal(ValueEntity_.attributeSet, attributeSet));
			for (ValueEntity value : valueList) {
				logger.debug("Try to delete entry {}", value.getId());
				valueDao.deleteAllDependingValues(value);
			}
		}
	}

	private void deleteAllRolesForUser(UserEntity user, String executor, UserDeleteAuditor auditor) {
		List<RoleEntity> roleList = roleDao.findByUser(user);
		for (RoleEntity role : roleList) {
			logger.info("Delete all personal user data: Remove user {} grom role {}", user.getId(), role.getName());
			roleDao.deleteUserRole(user.getId(), role.getName());
		}
	}

	private void deleteAllGroupsForUser(UserEntity user, String executor, UserDeleteAuditor auditor) {
		List<GroupEntity> groupList = groupDao.findByUser(user);
		for (GroupEntity group : groupList) {
			if (!(group instanceof LocalProjectGroupEntity)) {
				logger.info("Delete all personal user data: Remove user {} from group {}", user.getId(), group.getId());
				groupDao.removeUserGromGroup(user, group);
			} else {
				logger.info("Delete all personal user data: Not removing user {} from group {}. It's project group",
						user.getId(), group.getId());
			}
		}
	}

	private void deleteAllRegistriesForUser(UserEntity user, String executor, UserDeleteAuditor auditor) {
		List<RegistryEntity> registryList = registryDao.findByUser(user);
		for (RegistryEntity registry : registryList) {
			deleteRegistry(registry, user.getIdentity(), executor, auditor);
		}

		for (RegistryEntity registry : registryList) {
			logger.info("Delete all personal user data: Scrubbing registry {}", registry.getId());
			registry.getRegistryValues().clear();
		}
	}

	private void deleteRegistry(RegistryEntity registry, IdentityEntity identity, String executor,
			UserDeleteAuditor auditor) {

		logger.info("Delete all personal user data: Check registry {} for deregister", registry.getId());
		/*
		 * Deregister Services with ACTIVE, LOST_ACCESS or ON_HOLD first
		 */
		if ((RegistryStatus.ACTIVE == registry.getRegistryStatus())
				|| (RegistryStatus.LOST_ACCESS == registry.getRegistryStatus())
				|| (RegistryStatus.ON_HOLD == registry.getRegistryStatus())) {
			try {
				HashMap<String, String> map = new HashMap<String, String>();
				registry.getRegistryValues().forEach((k, v) -> map.put(k, v));
				registrator.deregisterUser(registry, executor, auditor, "delete-all-user-data");

				// Inform all services with the old registry values, if needed
				map.put("identity_uidnumber", "" + identity.getUidNumber());
				map.put("identity_generatedusername", identity.getGeneratedLocalUsername());
				GenericMapDataEvent event = new GenericMapDataEvent(map);
				List<EventEntity> eventList = new ArrayList<EventEntity>(
						serviceEventDao.findAllByService(registry.getService()));
				try {
					eventSubmitter.submit(event, eventList, EventType.SERVICE_DEREGISTER_DELETE_ALL, executor);
				} catch (EventSubmitException e) {
					logger.warn("Could not submit event", e);
				}
			} catch (RegisterException e) {
				logger.warn("Exception while deregister user", e);
			}
		}

		if (RegistryStatus.PENDING == registry.getRegistryStatus()) {
			try {
				approvor.denyApproval(registry, executor, auditor);
			} catch (RegisterException e) {
				logger.warn("Exception while deny approval", e);
			}
		}
	}
}
