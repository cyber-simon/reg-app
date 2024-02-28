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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.notEqual;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.or;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryEntity_;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity_;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class IdentityServiceImpl extends BaseServiceImpl<IdentityEntity> implements IdentityService {

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
	public List<IdentityEntity> findByMissingPreferredUser(int limit) {
		return dao.findAll(withLimit(limit), isNull(IdentityEntity_.prefUser));
	}

	@Override
	public void setPreferredUser(IdentityEntity identity) {
		if (identity.getPrefUser() == null) {
			identity = dao.fetch(identity.getId());

			UserEntity oldest = null;
			Date date = new Date();
			for (UserEntity user : identity.getUsers()) {
				if (user.getCreatedAt().before(date)) {
					date = user.getCreatedAt();
					oldest = user;
				}
			}
			logger.debug("Setting pref user {} for identity {}", oldest.getId(), identity.getId());
			identity.setPrefUser(oldest);
		}
	}

	@Override
	public void createMissingIdentities() {
		logger.info("Creating missing identities...");

		List<UserEntity> userList = findUsersWithMissingIdentity();
		for (UserEntity user : userList) {
			logger.info("Creating identity for user {}", user.getId());
			IdentityEntity id = dao.createNew();
			id.setTwoFaUserId(user.getId().toString());
			id = dao.persist(id);
			user.setIdentity(id);
		}
		logger.info("Creating missing identities done.");

		logger.info("Migrate registries from users to identities...");

		List<RegistryEntity> registriesWithMissingIdentities = registryDao
				.findAll(isNull(RegistryEntity_.identity));
		for (RegistryEntity registry : registriesWithMissingIdentities) {
			logger.info("Migrate regsitry for user {}", registry.getUser().getId());
			registry.setIdentity(registry.getUser().getIdentity());
		}
		logger.info("Migrate registries from users to identities done.");

		logger.info("Add missing 2fa userIds...");
		for (IdentityEntity id : findUserIdsWithMissingTwoFa()) {
			Set<UserEntity> users = id.getUsers();

			if (users == null) {
				logger.warn("Identity {} has no users", id.getId());
			} else if (users.size() == 0) {
				logger.warn("Identity {} has no users", id.getId());
			} else if (users.size() == 1) {
				for (UserEntity user : users) {
					id.setTwoFaUserId(user.getId().toString());
					if (user.getEppn() != null)
						id.setTwoFaUserName(user.getEppn());
					else
						id.setTwoFaUserName(UUID.randomUUID().toString());

					logger.info("Add missing 2fa userId {} and 2fa username {}", id.getTwoFaUserId(),
							id.getTwoFaUserName());
				}
			} else {
				logger.warn(
						"Add missing 2fa user id from identity with more than one account is not supported! Check identity {}",
						id.getId());
			}
		}
		logger.info("Add missing 2fa userIds done.");

		logger.info("Add missing uidNumbers...");
		for (IdentityEntity id : findUserIdsWithMissingUidNumber()) {
			Set<UserEntity> users = id.getUsers();

			if (users == null) {
				logger.warn("Identity {} has no users", id.getId());
			} else if (users.size() == 0) {
				logger.warn("Identity {} has no users", id.getId());
			} else {
				UserEntity user = users.stream().min(Comparator.comparing(UserEntity::getUidNumber)).orElse(null);
				if (user != null) {
					id.setUidNumber(user.getUidNumber());
					logger.info("Add missing uidNumber {}", id.getUidNumber());
				}
			}
		}
		logger.info("Add missing uidNumbers done.");

		logger.info("Migrate ssh pub keys from users to identities...");

		List<SshPubKeyEntity> keyList = findKeysWithMissingIdentity();
		for (SshPubKeyEntity key : keyList) {
			// Keys can have no user. These are blacklisted key, we can ignore them
			if (key.getUser() != null) {
				logger.info("Migrate ssh key for user {}", key.getUser().getId());
				key.setIdentity(key.getUser().getIdentity());
			}
		}
		logger.info("Migrate ssh pub keys from users to identities done.");

	}

	private List<IdentityEntity> findUserIdsWithMissingTwoFa() {
		return dao.findAll(or(isNull(IdentityEntity_.twoFaUserId), isNull(IdentityEntity_.twoFaUserName)));
	}

	private List<IdentityEntity> findUserIdsWithMissingUidNumber() {
		return dao.findAll(isNull(IdentityEntity_.uidNumber));
	}

	private List<UserEntity> findUsersWithMissingIdentity() {
		return userDao.findAll(and(isNull(UserEntity_.identity), notEqual(UserEntity_.userStatus, UserStatus.DEREGISTERED)));
	}

	private List<SshPubKeyEntity> findKeysWithMissingIdentity() {
		return sshPubKeyDao.findAll(isNull(SshPubKeyEntity_.identity));
	}

	@Override
	protected BaseDao<IdentityEntity> getDao() {
		return dao;
	}

}
