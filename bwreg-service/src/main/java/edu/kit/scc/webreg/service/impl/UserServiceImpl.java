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

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.opensaml.saml.saml2.core.Assertion;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserService;

@Stateless
public class UserServiceImpl extends BaseServiceImpl<UserEntity> implements UserService, Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private Logger logger;
	
	@Inject
	private UserDao dao;
	
	@Inject
	private SamlUserDao samlUserDao;
	
	@Inject
	private UserUpdater userUpdater;
	
	@Inject 
	private RegistryDao registryDao;
	
	@Inject
	private UserLoginInfoDao userLoginInfoDao;
	
	@Inject
	private IdentityDao identityDao;
	
	@Override
	public UserLoginInfoEntity addLoginInfo(Long userId, UserLoginMethod method, UserLoginInfoStatus status, String from) {
		UserEntity user = dao.findById(userId);
		UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
		loginInfo.setUser(user);
		loginInfo.setIdentity(user.getIdentity());
		loginInfo.setLoginDate(new Date());
		loginInfo.setLoginMethod(method);
		loginInfo.setLoginStatus(status);
		loginInfo.setFrom(from);
		loginInfo = userLoginInfoDao.persist(loginInfo);
		return loginInfo;
	}

	@Override
	public UserLoginInfoEntity addLoginInfo(IdentityEntity identity, UserLoginMethod method, UserLoginInfoStatus status, String from) {
		identity = identityDao.merge(identity);
		UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
		loginInfo.setIdentity(identity);
		loginInfo.setLoginDate(new Date());
		loginInfo.setLoginMethod(method);
		loginInfo.setLoginStatus(status);
		loginInfo.setFrom(from);
		loginInfo = userLoginInfoDao.persist(loginInfo);
		return loginInfo;
	}

	@Override
	public SamlUserEntity findByPersistent(String spId, String idpId, String persistentId) {
		return samlUserDao.findByPersistent(spId, idpId, persistentId);
	}
	
	@Override
	public List<UserEntity> findByIdentity(IdentityEntity identity) {
		return dao.findByIdentity(identity);
	}

	@Override
	public List<UserEntity> findOrderByUpdatedWithLimit(Date date, Integer limit) {
		return dao.findOrderByUpdatedWithLimit(date, limit);
	}

	@Override
	public List<UserEntity> findOrderByFailedUpdateWithLimit(Date date, Integer limit) {
		return dao.findOrderByFailedUpdateWithLimit(date, limit);
	}

    @Override
	public List<UserEntity> findGenericStoreKeyWithLimit(String key, Integer limit) {
		return dao.findGenericStoreKeyWithLimit(key, limit);
	}

	@Override
	public List<UserEntity> findByGroup(GroupEntity group) {
		return dao.findByGroup(group);
	}
	
	@Override
	public UserEntity findByUidNumber(Long uidNumber) {
		return dao.findByUidNumber(uidNumber);
	}

	@Override
	public List<UserEntity> findByEppn(String eppn) {
		return dao.findByEppn(eppn);
	}

	@Override
	public List<UserEntity> findByStatus(UserStatus status) {
		return dao.findByStatus(status);
	}
	
	@Override
	public UserEntity findByIdWithAll(Long id) {
		return dao.findByIdWithAll(id);
	}

	@Override
	public UserEntity findByIdWithStore(Long id) {
		return dao.findByIdWithStore(id);
	}

	@Override
	public SamlUserEntity updateUserFromIdp(SamlUserEntity user, String executor, StringBuffer debugLog) 
			throws UserUpdateException {
		return userUpdater.updateUserFromIdp(user, null, executor, debugLog);
	}

	@Override
	public SamlUserEntity updateUserFromIdp(SamlUserEntity user, String executor) 
			throws UserUpdateException {
		return userUpdater.updateUserFromIdp(user, null, executor, null);
	}

	@Override
	public SamlUserEntity updateUserFromIdp(SamlUserEntity user, ServiceEntity service, String executor) 
			throws UserUpdateException {
		return userUpdater.updateUserFromIdp(user, executor);
	}

	@Override
	public SamlUserEntity updateUserFromAssertion(SamlUserEntity user, Assertion assertion, String executor) 
				throws UserUpdateException {
		return userUpdater.updateUser(user, assertion, executor);
	}

	@Override
	protected BaseDao<UserEntity> getDao() {
		return dao;
	}
	
	@Override
	public void checkOnHoldRegistries(UserEntity user) {
		if (user.getUserStatus().equals(UserStatus.ON_HOLD)) {
			List<RegistryEntity> registryList = registryDao.findByUserAndStatus(user, 
					RegistryStatus.ACTIVE, RegistryStatus.LOST_ACCESS, RegistryStatus.INVALID);
			for (RegistryEntity registry : registryList) {
				logger.debug("Setting registry {} (user {}) ON_HOLD", registry.getId(), user.getEppn()); 
				registry.setRegistryStatus(RegistryStatus.ON_HOLD);
				registry.setStatusMessage("user-on-hold");
				registry.setLastStatusChange(new Date());
			}			
		}
	}
}
