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
package edu.kit.scc.webreg.bean.admin;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.util.ViewIds;

@Named("addAdminUserBean")
@RequestScoped
public class AddAdminUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AdminUserService service;

	private AdminUserEntity entity;
	
	@PostConstruct
	public void init() {
		entity = service.createNew();
	}
	
	public String save() {
		entity = service.save(entity);
		return ViewIds.SHOW_ADMIN_USER + "?id=" + entity.getId() + "&faces-redirect=true";
	}

	public AdminUserEntity getEntity() {
		return entity;
	}

	public void setEntity(AdminUserEntity entity) {
		this.entity = entity;
	}
}
