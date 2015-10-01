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

import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public interface UserService extends BaseService<UserEntity, Long> {

	UserEntity findByPersistentWithRoles(String spId, String idpId,
			String persistentId);
	
	UserEntity findByEppn(String eppn);
	
	UserEntity findByIdWithAll(Long id);
	
	boolean updateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor) throws UserUpdateException;
	
	boolean updateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, boolean withoutUidNumber, Auditor auditor)
			throws UserUpdateException;

	void convertLegacyUsers();

	UserEntity findByIdWithStore(Long id);

	List<UserEntity> findByGroup(GroupEntity group);

	List<UserEntity> findOrderByUpdatedWithLimit(Date date, Integer limit);

	List<UserEntity> findGenericStoreKeyWithLimit(String key, Integer limit);

	List<UserEntity> findOrderByFailedUpdateWithLimit(Date date, Integer limit);

	UserEntity updateUserFromIdp(UserEntity user) throws UserUpdateException;

	UserEntity updateUserFromIdp(UserEntity user, ServiceEntity service)
			throws UserUpdateException;

	UserEntity updateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, String executor)
			throws UserUpdateException;
}
