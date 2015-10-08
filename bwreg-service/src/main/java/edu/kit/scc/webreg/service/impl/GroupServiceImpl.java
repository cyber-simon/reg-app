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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.reg.GroupUtil;

@Stateless
public class GroupServiceImpl extends BaseServiceImpl<GroupEntity, Long> implements GroupService {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupDao groupDao;
	
	@Inject
	private GroupUtil groupUtil;
	
	@Inject
	private UserDao userDao;
	
	@Override
	public void updateGroupMembers(GroupEntity group, Set<UserEntity> newMembers) {
		Set<UserEntity> oldMembers = new HashSet<UserEntity>(userDao.findByGroup(group));

		Set<UserEntity> usersToAdd = new HashSet<UserEntity>(newMembers);
		usersToAdd.removeAll(oldMembers);
		for (UserEntity user : usersToAdd) {
			user = userDao.merge(user);
			group = groupDao.merge(group);
			groupDao.addUserToGroup(user, group);
		}
		
		Set<UserEntity> usersToRemove = new HashSet<UserEntity>(oldMembers);
		usersToRemove.removeAll(newMembers);
		for (UserEntity user : usersToRemove) { 
			user = userDao.merge(user);
			group = groupDao.merge(group);
			groupDao.removeUserGromGroup(user, group);
		}
	}
	
	@Override
	public void addUserToGroup(UserEntity user, GroupEntity group) {
		groupDao.addUserToGroup(user, group);
	}	
	
	@Override
	public void removeUserGromGroup(UserEntity user, GroupEntity group) {
		groupDao.removeUserGromGroup(user, group);
	}	
	
	@Override
	public GroupEntity findWithUsers(Long id) {
		return groupDao.findWithUsers(id);
	}	
	
	@Override
	public GroupEntity findByName(String name) {
		return groupDao.findByName(name);
	}	
	
	@Override
	public Set<UserEntity> getEffectiveMembers(GroupEntity group) {
		group = groupDao.findById(group.getId());
		return groupUtil.rollUsersForGroup(group);
	}
	
	@Override
	public List<GroupEntity> findByUser(UserEntity user) {
		return groupDao.findByUser(user);
	}	

	@Override
	public Set<GroupEntity> findByUserWithParents(UserEntity user) {
		Set<GroupEntity> groups = new HashSet<GroupEntity>(groupDao.findByUser(user));
		Set<GroupEntity> targetGroups = new HashSet<GroupEntity>();
		rollParents(targetGroups, groups, 0, 3);
		return groups;
	}	

	private void rollParents(Set<GroupEntity> targetGroups, Set<GroupEntity> groups, int depth, int maxDepth) {
		if (depth <= maxDepth) {
			for (GroupEntity group : groups) {
				rollParents(targetGroups, group.getParents(), depth + 1, maxDepth);
				targetGroups.add(group);
			}		
		}
	}
	
	@Override
	protected BaseDao<GroupEntity, Long> getDao() {
		return groupDao;
	}	
}
