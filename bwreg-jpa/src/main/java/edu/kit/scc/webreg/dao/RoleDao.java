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

import java.util.List;
import java.util.Set;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.RoleGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserRoleEntity;

public interface RoleDao extends BaseDao<RoleEntity, Long> {

	RoleEntity findByName(String name);

	RoleEntity findWithUsers(Long id);

	List<RoleEntity> findByUser(UserEntity user);

	UserRoleEntity createNewUserRole();

	void persistUserRole(UserRoleEntity userRole);

	List<RoleEntity> findByUserId(Long userId);

	void deleteUserRole(Long userId, String roleName);

	Boolean checkUserInRole(Long userId, String roleName);

	Boolean checkAdminUserInRole(Long userId, String roleName);

	void addGroupToRole(GroupEntity group, RoleEntity role);

	void removeGroupFromRole(GroupEntity group, RoleEntity role);

	RoleGroupEntity createNewRoleGroup();

	RoleGroupEntity findRoleGroupEntity(GroupEntity group, RoleEntity role);

	List<RoleEntity> findByGroups(Set<GroupEntity> groups);

	void addUserToRole(UserEntity user, String roleName);

	List<UserEntity> findUsersForRole(RoleEntity role);

	List<GroupEntity> findGroupsForRole(RoleEntity role);
	
}
