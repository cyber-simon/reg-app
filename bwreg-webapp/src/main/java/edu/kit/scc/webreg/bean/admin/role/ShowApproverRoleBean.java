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

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.ApproverRoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.ApproverRoleService;

@Named("showApproverRoleBean")
@RequestScoped
public class ShowApproverRoleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApproverRoleService service;

	private ApproverRoleEntity entity;

	private List<UserEntity> userInRoleList;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.findWithUsers(id);
	}
	
	public ApproverRoleEntity getEntity() {
		return entity;
	}

	public void setEntity(ApproverRoleEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<UserEntity> getUserInRoleList() {
		return userInRoleList;
	}

	public void setUserInRoleList(List<UserEntity> userInRoleList) {
		this.userInRoleList = userInRoleList;
	}
}
