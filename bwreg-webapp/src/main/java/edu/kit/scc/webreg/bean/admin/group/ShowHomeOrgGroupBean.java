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
package edu.kit.scc.webreg.bean.admin.group;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.HomeOrgGroupService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;

@Named
@ViewScoped
public class ShowHomeOrgGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupService service;
	
	@Inject
	private HomeOrgGroupService groupService;

	@Inject
	private ServiceGroupFlagService groupFlagService;

	private HomeOrgGroupEntity entity;

	private List<ServiceGroupFlagEntity> groupFlagList;
	private List<UserEntity> memberList;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = groupService.findWithUsers(id);
			groupFlagList = groupFlagService.findByGroup(entity);
			memberList = new ArrayList<UserEntity>();
			for (UserGroupEntity ug : entity.getUsers()) {
				memberList.add(ug.getUser());
			}			
		}
	}

	public void addGroupFlags() {
		entity = (HomeOrgGroupEntity) service.persistWithServiceFlags(entity);
		groupFlagList = groupFlagService.findByGroup(entity);
	}
	
	public HomeOrgGroupEntity getEntity() {
		return entity;
	}

	public void setEntity(HomeOrgGroupEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ServiceGroupFlagEntity> getGroupFlagList() {
		return groupFlagList;
	}

	public List<UserEntity> getMemberList() {
		return memberList;
	}
}
