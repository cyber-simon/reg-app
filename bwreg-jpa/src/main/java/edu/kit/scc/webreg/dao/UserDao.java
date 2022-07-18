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
package edu.kit.scc.webreg.dao;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import java.util.Date;
import java.util.List;

public interface UserDao extends BaseDao<UserEntity> {

	List<UserEntity> findByEppn(String eppn);
	UserEntity findByIdWithAll(Long id);
	List<UserEntity> findLegacyUsers();
	List<UserEntity> findByPrimaryGroup(GroupEntity group);
	UserEntity findByIdWithStore(Long id);
	List<UserEntity> findByGroup(GroupEntity group);
	List<UserEntity> findOrderByUpdatedWithLimit(Date date, Integer limit);
	List<UserEntity> findGenericStoreKeyWithLimit(String key, Integer limit);
	List<UserEntity> findOrderByFailedUpdateWithLimit(Date date, Integer limit);
	List<UserEntity> findByStatus(UserStatus status);
	List<UserEntity> findByStatusAndTimeSince(UserStatus status, Long statusSince, Integer limit);
	UserEntity findByUidNumber(Long uidNumber);
	List<UserEntity> findMissingIdentity();
	List<UserEntity> findByIdentity(IdentityEntity identity);
	List<UserEntity> findScheduledUsers(Integer limit);
	List<UserEntity> findByAttribute(String key, String value);
	List<UserEntity> findByGeneric(String key, String value);
}
