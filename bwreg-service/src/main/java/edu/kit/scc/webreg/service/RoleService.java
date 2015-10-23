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
import java.util.Set;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface RoleService extends BaseService<RoleEntity, Long> {

	RoleEntity findByName(String name);

	RoleEntity findWithUsers(Long id);

	List<RoleEntity> findByUser(UserEntity user);

	void addUserToRole(UserEntity user, String roleName);

	List<RoleEntity> findByUserId(Long userId);

	void removeUserFromRole(UserEntity user, String roleName);

	Boolean checkUserInRole(Long userId, String roleName);

	Boolean checkAdminUserInRole(Long userId, String roleName);

	List<RoleEntity> findByGroups(Set<GroupEntity> groups);

	List<UserEntity> findUsersForRole(RoleEntity role);

	List<GroupEntity> findGroupsForRole(RoleEntity role);

	void addGroupToRole(GroupEntity group, RoleEntity role);

	void removeGroupFromRole(GroupEntity group, RoleEntity role);

}
