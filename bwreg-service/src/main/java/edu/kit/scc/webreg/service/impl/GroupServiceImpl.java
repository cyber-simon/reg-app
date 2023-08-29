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

import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

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
public class GroupServiceImpl extends BaseServiceImpl<GroupEntity> implements GroupService {

	private static final long serialVersionUID = 1L;

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
		group = groupDao.fetch(group.getId());
		user = userDao.fetch(user.getId());
		groupUpdater.addUserToGroup(user, group, emitUpdate);
	}

	@Override
	public void removeUserGromGroup(UserEntity user, GroupEntity group, boolean emitUpdate) {
		group = groupDao.fetch(group.getId());
		user = userDao.fetch(user.getId());
		groupUpdater.removeUserFromGroup(user, group, emitUpdate);
	}

	@Override
	public GroupEntity findByName(String name) {
		return groupDao.findByName(name);
	}

	@Override
	public Set<UserEntity> getEffectiveMembers(GroupEntity group) {
		group = groupDao.fetch(group.getId());
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
		return groupDao.findByUserWithChildren(user);
	}

	@Override
	protected BaseDao<GroupEntity> getDao() {
		return groupDao;
	}
}
