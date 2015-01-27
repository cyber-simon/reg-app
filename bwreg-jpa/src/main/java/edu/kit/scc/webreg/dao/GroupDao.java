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

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;

public interface GroupDao extends BaseDao<GroupEntity, Long> {

	GroupEntity findByGidNumber(Integer gid);

	GroupEntity findByName(String name);

	GroupEntity findByNameAndPrefix(String name, String prefix);

	List<GroupEntity> findByUser(UserEntity user);

	GroupEntity findWithUsers(Long id);

	void addUserToGroup(UserEntity user, GroupEntity group);

	void removeUserGromGroup(UserEntity user, GroupEntity group);

	UserGroupEntity createNewUserGroup();

	boolean isUserInGroup(UserEntity user, GroupEntity group);

	UserGroupEntity findUserGroupEntity(UserEntity user, GroupEntity group);

	LocalGroupEntity findLocalGroupByName(String name);

	LocalGroupDao getLocalGroupDao();

	HomeOrgGroupDao getHomeOrgGroupDao();

}
