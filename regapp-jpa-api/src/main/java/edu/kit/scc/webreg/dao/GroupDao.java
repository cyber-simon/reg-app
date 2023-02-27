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

import edu.kit.scc.webreg.dao.as.AttributeSourceGroupDao;
import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;

public interface GroupDao extends BaseDao<GroupEntity> {

	GroupEntity findByName(String name);

	List<GroupEntity> findByUser(UserEntity user);

	List<GroupEntity> findByUser(PaginateBy paginateBy, UserEntity user);

	List<GroupEntity> findByUserId(PaginateBy paginateBy, Long userId);

	void addUserToGroup(UserEntity user, GroupEntity group);

	void removeUserGromGroup(UserEntity user, GroupEntity group);

	boolean isUserInGroup(UserEntity user, GroupEntity group);

	UserGroupEntity findUserGroupEntity(UserEntity user, GroupEntity group);

	LocalGroupDao getLocalGroupDao();

	HomeOrgGroupDao getHomeOrgGroupDao();

	AttributeSourceGroupDao getAttributeSourceGroupDao();

	Set<GroupEntity> findByUserWithChildren(UserEntity user);

	Long getNextGID();

	ServiceBasedGroupEntity persistWithServiceFlags(ServiceBasedGroupEntity entity);

	ServiceBasedGroupEntity persistWithServiceFlags(ServiceBasedGroupEntity entity, Set<ServiceEntity> services);

	void setServiceFlags(ServiceBasedGroupEntity entity, ServiceGroupStatus status);

	Number countAllByUserId(Long userId);

	GroupEntity findByGidNumber(Integer gid);

	List<UserEntity> getUsersOfGroup(Long groupId);

	List<GroupEntity> getChildrenOfGroup(Long groupId);

}
