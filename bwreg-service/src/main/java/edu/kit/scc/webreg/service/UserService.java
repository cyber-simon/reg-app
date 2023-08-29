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
package edu.kit.scc.webreg.service;

import java.util.List;

import org.opensaml.saml.saml2.core.Assertion;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public interface UserService extends BaseService<UserEntity> {

	List<UserEntity> findByEppn(String eppn);

	UserEntity findByIdWithAll(Long id);

	UserEntity findByIdWithStore(Long id);

	List<UserEntity> findByGroup(GroupEntity group);

	SamlUserEntity updateUserFromIdp(SamlUserEntity user, String executor) throws UserUpdateException;

	List<UserEntity> findByStatus(UserStatus status);

	List<UserEntity> findByStatusAndTimeSince(UserStatus status, Long statusSince, Integer limit);

	void checkOnHoldRegistries(UserEntity user);

	SamlUserEntity updateUserFromAssertion(SamlUserEntity user, Assertion assertion, String executor)
			throws UserUpdateException;

	UserLoginInfoEntity addLoginInfo(Long userId, UserLoginMethod method, UserLoginInfoStatus status, String from);

	List<UserEntity> findByIdentity(IdentityEntity identity);

	UserLoginInfoEntity addLoginInfo(IdentityEntity identity, UserLoginMethod method, UserLoginInfoStatus status,
			String from);

	SamlUserEntity updateUserFromIdp(SamlUserEntity user, String executor, StringBuffer debugLog)
			throws UserUpdateException;

	SamlUserEntity findByPersistent(String spId, String idpId, String persistentId);

}
