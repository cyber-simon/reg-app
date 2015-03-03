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
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.RoleService;

@ManagedBean
@ViewScoped
public class ShowAdminUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AdminUserService adminUserService;

	@Inject
	private RoleService roleService;
	
	private AdminUserEntity entity;

	private DualListModel<RoleEntity> roleList;
	
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = adminUserService.findByIdWithAttrs(id, "roles");
			roleList = new DualListModel<RoleEntity>();

			List<RoleEntity> targetList = new ArrayList<RoleEntity>(entity.getRoles());
			List<RoleEntity> sourceList = roleService.findAll();

			sourceList.removeAll(targetList);

			roleList.setSource(sourceList);
			roleList.setTarget(targetList);
		}
	}

	public void onTransfer(TransferEvent event) {
		entity = adminUserService.findByIdWithAttrs(id, "roles");
		if (event.isAdd()) {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				entity.getRoles().add(role);
				entity = adminUserService.save(entity);
			}
		}
		else {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				entity.getRoles().remove(role);
				entity = adminUserService.save(entity);
			}
		}
		entity = adminUserService.findByIdWithAttrs(id, "roles");
	}

	public AdminUserEntity getEntity() {
		return entity;
	}

	public void setEntity(AdminUserEntity entity) {
		this.entity = entity;
	}

	public DualListModel<RoleEntity> getRoleList() {
		return roleList;
	}

	public void setRoleList(DualListModel<RoleEntity> roleList) {
		this.roleList = roleList;
	}
	
}
