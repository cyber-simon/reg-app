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
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface GroupService extends BaseService<GroupEntity, Long> {

	GroupEntity findByName(String name);

	List<GroupEntity> findByUser(UserEntity user);

	GroupEntity findWithUsers(Long id);

	Set<UserEntity> getEffectiveMembers(GroupEntity group);

	void updateGroupMembers(GroupEntity group, Set<UserEntity> newMembers);

	void addUserToGroup(UserEntity user, GroupEntity group);

	void removeUserGromGroup(UserEntity user, GroupEntity group);

	Set<GroupEntity> findByUserWithChildren(UserEntity user);

	ServiceBasedGroupEntity persistWithServiceFlags(
			ServiceBasedGroupEntity entity);

}
