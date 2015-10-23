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
package edu.kit.scc.webreg.bean.admin.role;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.UserService;

@ManagedBean
@ViewScoped
public class ShowRoleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private RoleService service;

	@Inject
	private UserService userService;
	
	@Inject
	private GroupService groupService;
	
	private RoleEntity entity;
	private List<UserEntity> userList;
	private List<GroupEntity> groupList;

	private LazyDataModel<UserEntity> allUserList;
	private LazyDataModel<GroupEntity> allGroupList;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(id);
			userList = service.findUsersForRole(entity);
			groupList = service.findGroupsForRole(entity);

			allUserList = new GenericLazyDataModelImpl<UserEntity, UserService, Long>(userService);
			allGroupList = new GenericLazyDataModelImpl<GroupEntity, GroupService, Long>(groupService);
		}
	}
	
	public void removeUserFromRole(UserEntity user) {
		service.removeUserFromRole(user, entity.getName());
		userList = service.findUsersForRole(entity);
	}

	public void removeGroupFromRole(GroupEntity group) {
		service.removeGroupFromRole(group, entity);
		groupList = service.findGroupsForRole(entity);
	}
	
	public void addUserToRole(UserEntity user) {
		service.addUserToRole(user, entity.getName());
		userList = service.findUsersForRole(entity);
	}

	public void addGroupToRole(GroupEntity group) {
		service.addGroupToRole(group, entity);
		groupList = service.findGroupsForRole(entity);
	}
	
	public RoleEntity getEntity() {
		return entity;
	}

	public void setEntity(RoleEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<UserEntity> getUserList() {
		return userList;
	}

	public List<GroupEntity> getGroupList() {
		return groupList;
	}

	public LazyDataModel<UserEntity> getAllUserList() {
		return allUserList;
	}

	public LazyDataModel<GroupEntity> getAllGroupList() {
		return allGroupList;
	}
}
