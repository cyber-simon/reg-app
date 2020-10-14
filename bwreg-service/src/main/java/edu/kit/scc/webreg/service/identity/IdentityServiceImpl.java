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
package edu.kit.scc.webreg.service.identity;

import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class IdentityServiceImpl extends BaseServiceImpl<IdentityEntity, Long> implements IdentityService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private IdentityDao dao;

	@Inject
	private UserDao userDao;
	
	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private SshPubKeyDao sshPubKeyDao;
	
	@Override
	public void createMissingIdentities() {
		logger.info("Creating missing identities...");

		List<UserEntity> userList = userDao.findMissingIdentity();
		for (UserEntity user : userList) {
			logger.info("Creating identity for user {}", user.getId());
			IdentityEntity id = dao.createNew();
			id.setTwoFaUserId(user.getId().toString());
			id = dao.persist(id);
			user.setIdentity(id);
		}
		logger.info("Creating missing identities done.");
		
		logger.info("Migrate registries from users to identities...");

		List<RegistryEntity> registryList = registryDao.findMissingIdentity();
		for (RegistryEntity registry : registryList) {
			logger.info("Migrate regsitry for user {}", registry.getUser().getId());
			registry.setIdentity(registry.getUser().getIdentity());
		}
		logger.info("Migrate registries from users to identities done.");
		
		logger.info("Add missing 2fa userIds...");
		List<IdentityEntity> idList = dao.findMissingTwoFaUserId();
		for (IdentityEntity id : idList) {
			Set<UserEntity> users = id.getUsers();
			
			if (users.size() == 1) {
				for(UserEntity user : users) {
					id.setTwoFaUserId(user.getId().toString());
					id.setTwoFaUserName(user.getEppn());
				}			
			}
			else {
				logger.warn("Add missing 2fa user id from identity with more than one account is not supported! Check identity {}" + id.getId());
			}
		}
		logger.info("Add missing 2fa userIds done.");
		
		logger.info("Migrate ssh pub keys from users to identities...");

		List<SshPubKeyEntity> keyList = sshPubKeyDao.findMissingIdentity();
		for (SshPubKeyEntity key : keyList) {
			// Keys can have no user. These are blacklisted key, we can ignore them
			if (key.getUser() != null) {
				logger.info("Migrate ssh key for user {}", key.getUser().getId());
				key.setIdentity(key.getUser().getIdentity());
			}
		}
		logger.info("Migrate ssh pub keys from users to identities done.");

	}

	@Override
	protected BaseDao<IdentityEntity, Long> getDao() {
		return dao;
	}

}
