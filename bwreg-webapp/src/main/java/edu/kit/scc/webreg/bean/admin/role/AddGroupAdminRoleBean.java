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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;
import edu.kit.scc.webreg.service.GroupAdminRoleService;
import edu.kit.scc.webreg.util.ViewIds;

@Named("addGroupAdminRoleBean")
@RequestScoped
public class AddGroupAdminRoleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupAdminRoleService roleService;
	
	private GroupAdminRoleEntity entity;
	
	@PostConstruct
	public void init() {
		entity = roleService.createNew();
	}
	
	public String save() {
		roleService.save(entity);
		return ViewIds.LIST_ROLES;
	}

	public GroupAdminRoleEntity getEntity() {
		return entity;
	}

	public void setEntity(GroupAdminRoleEntity entity) {
		this.entity = entity;
	}
}
