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
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.UserDeleteAuditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.UserDeleteService;
import edu.kit.scc.webreg.service.reg.impl.Approvor;
import edu.kit.scc.webreg.service.reg.impl.Registrator;

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

	@Override
	public void deleteUserData(IdentityEntity identity, String executor) {
		logger.info("Delete all personal user data for identity {}", identity.getId());
		identity = identityDao.merge(identity);
		
		for (UserEntity user : identity.getUsers()) {
			logger.info("Delete all personal user data for user {}", user.getId());

			UserDeleteAuditor auditor = new UserDeleteAuditor(auditEntryDao, auditDetailDao, appConfig);
			auditor.startAuditTrail(executor, true);
			auditor.setName("UserDelete-Audit");
			auditor.setDetail("delete all personal data for user " + user.getEppn() + " " + user.getId());
			auditor.setUser(user);
			
			List<RegistryEntity> registryList = registryDao.findByUser(user);
			for (RegistryEntity registry : registryList) {
				
				logger.info("Delete all personal user data: Check registry {} for deregister", registry.getId());
				/*
				 * Deregister Services with ACTIVE, LOST_ACCESS or ON_HOLD first
				 */
				if ((RegistryStatus.ACTIVE == registry.getRegistryStatus()) ||
						(RegistryStatus.LOST_ACCESS == registry.getRegistryStatus()) ||
						(RegistryStatus.ON_HOLD == registry.getRegistryStatus())) {
					try {
						registrator.deregisterUser(registry, executor, auditor);
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

			for (RegistryEntity registry : registryList) {
				logger.info("Delete all personal user data: Scrubbing registry {}", registry.getId());
				registry.getRegistryValues().clear();
			}
			
			List<GroupEntity> groupList = groupDao.findByUser(user);
			for (GroupEntity group : groupList) {
				logger.info("Delete all personal user data: Remove user {} grom group {}", user.getId(), group.getId());
				groupDao.removeUserGromGroup(user, group);
			}
			
			user.getGenericStore().clear();
			user.getAttributeStore().clear();
			user.getUserAttrs().clear();
			user.getEmailAddresses().clear();
			user.setEmail(null);
			user.setEppn(null);
			user.setGivenName(null);
			user.setSurName(null);
			if (user instanceof SamlUserEntity)
				((SamlUserEntity) user).setPersistentId(null);
			if (user instanceof OidcUserEntity) {
				((OidcUserEntity) user).setSubjectId(null);
				((OidcUserEntity) user).setIssuer(null);
			}
			user.setUidNumber(serialDao.next("uid-number-serial").intValue());
			user.setUserStatus(UserStatus.DEREGISTERED);
			user.setLastStatusChange(new Date());
			
			List<RoleEntity> roleList = roleDao.findByUser(user);
			for (RoleEntity role : roleList) {
				logger.info("Delete all personal user data: Remove user {} grom role {}", user.getId(), role.getName());
				roleDao.deleteUserRole(user.getId(), role.getName());
			}
			
			auditor.finishAuditTrail();
			auditor.commitAuditTrail();
		}
		
		identity.setTwoFaUserId(null);
		identity.setTwoFaUserName(null);
	}	
}
