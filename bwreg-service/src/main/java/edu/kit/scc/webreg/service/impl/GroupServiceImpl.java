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

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.group.GroupUpdater;
import edu.kit.scc.webreg.service.reg.GroupUtil;

@Stateless
public class GroupServiceImpl extends BaseServiceImpl<GroupEntity, Long> implements GroupService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private GroupDao groupDao;
	
	@Inject
	private GroupUtil groupUtil;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private GroupUpdater groupUpdater;
	
	@Override
	public void updateGroupMembers(GroupEntity group, Set<UserEntity> newMembers) {
		groupUpdater.updateGroupMembers(group, newMembers);
	}
	
	@Override
	public void addUserToGroup(UserEntity user, GroupEntity group, boolean emitUpdate) {
		group = groupDao.merge(group);
		user = userDao.merge(user);
		groupUpdater.addUserToGroup(user, group, emitUpdate);
	}	
	
	@Override
	public void removeUserGromGroup(UserEntity user, GroupEntity group, boolean emitUpdate) {
		group = groupDao.merge(group);
		user = userDao.merge(user);
		groupUpdater.removeUserFromGroup(user, group, emitUpdate);
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
	public ServiceBasedGroupEntity persistWithServiceFlags(ServiceBasedGroupEntity entity) {
		return groupDao.persistWithServiceFlags(entity);
	}	

	@Override
	public Set<GroupEntity> findByUserWithChildren(UserEntity user) {
		Set<GroupEntity> groups = new HashSet<GroupEntity>(groupDao.findByUser(user));
		Set<GroupEntity> targetGroups = new HashSet<GroupEntity>();
		rollChildren(targetGroups, groups, 0, 3);
		return targetGroups;
	}	

	private void rollChildren(Set<GroupEntity> targetGroups, Set<GroupEntity> groups, int depth, int maxDepth) {
		if (depth <= maxDepth) {
			for (GroupEntity group : groups) {
				if (logger.isTraceEnabled())
					logger.trace("Inspecting group {} with children count {}", group.getName(), group.getParents().size());
				rollChildren(targetGroups, group.getParents(), depth + 1, maxDepth);
				targetGroups.add(group);
			}		
		}
	}
	
	@Override
	protected BaseDao<GroupEntity, Long> getDao() {
		return groupDao;
	}	
}
