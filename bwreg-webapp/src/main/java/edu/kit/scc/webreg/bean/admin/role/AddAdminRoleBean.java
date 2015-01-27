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

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.AdminRoleEntity;
import edu.kit.scc.webreg.service.AdminRoleService;
import edu.kit.scc.webreg.util.ViewIds;

@Named("addAdminRoleBean")
@RequestScoped
public class AddAdminRoleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AdminRoleService roleService;
	
	private AdminRoleEntity entity;
	
	@PostConstruct
	public void init() {
		entity = roleService.createNew();
	}
	
	public String save() {
		roleService.save(entity);
		return ViewIds.LIST_ROLES;
	}

	public AdminRoleEntity getEntity() {
		return entity;
	}

	public void setEntity(AdminRoleEntity entity) {
		this.entity = entity;
	}
}
