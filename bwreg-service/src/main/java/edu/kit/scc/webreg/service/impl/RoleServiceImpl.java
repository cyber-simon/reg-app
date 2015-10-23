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
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.RoleService;

@Stateless
public class RoleServiceImpl extends BaseServiceImpl<RoleEntity, Long> implements RoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private RoleDao dao;

	@Override
	public void addUserToRole(UserEntity user, String roleName) {
		dao.addUserToRole(user, roleName);
	}
	
	@Override
	public void removeUserFromRole(UserEntity user, String roleName) {
		dao.deleteUserRole(user.getId(), roleName);
	}
	
	@Override
	public void addGroupToRole(GroupEntity group, RoleEntity role) {
		dao.addGroupToRole(group, role);
	}
	
	@Override
	public void removeGroupFromRole(GroupEntity group, RoleEntity role) {
		dao.removeGroupFromRole(group, role);
	}
	
	@Override
	public List<RoleEntity> findByUser(UserEntity user) {
		return dao.findByUser(user);
	}

	@Override
	public List<RoleEntity> findByGroups(Set<GroupEntity> groups) {
		return dao.findByGroups(groups);
	}

	@Override
	public List<RoleEntity> findByUserId(Long userId) {
		return dao.findByUserId(userId);
	}

	@Override
	public Boolean checkUserInRole(Long userId, String roleName) {
		return dao.checkUserInRole(userId, roleName);
	}
	
	@Override
	public Boolean checkAdminUserInRole(Long userId, String roleName) {
		return dao.checkAdminUserInRole(userId, roleName);
	}
	
	@Override
	public RoleEntity findWithUsers(Long id) {
		return dao.findWithUsers(id);
	}
	
	@Override
	public List<UserEntity> findUsersForRole(RoleEntity role) {
		return dao.findUsersForRole(role);
	}
	
	@Override
	public List<GroupEntity> findGroupsForRole(RoleEntity role) {
		return dao.findGroupsForRole(role);
	}
	
	@Override
	public RoleEntity findByName(String name) {
		return dao.findByName(name);
	}

	@Override
	protected BaseDao<RoleEntity, Long> getDao() {
		return dao;
	}
}
