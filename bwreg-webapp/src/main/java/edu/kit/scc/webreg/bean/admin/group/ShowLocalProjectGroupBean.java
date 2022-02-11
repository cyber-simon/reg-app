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
import java.util.List;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectGroupEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;

@Named
@ViewScoped
public class ShowLocalProjectGroupBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private GroupService groupService;
	
	@Inject
	private ServiceGroupFlagService groupFlagService;
	
	private LocalProjectGroupEntity entity;

	private List<ServiceGroupFlagEntity> groupFlagList;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ServiceGroupFlagEntity> getGroupFlagList() {
		if (groupFlagList == null) {
			groupFlagList = groupFlagService.findByGroup(entity);
		}
		return groupFlagList;
	}

	public LocalProjectGroupEntity getEntity() {
		if (entity == null) {
			entity = (LocalProjectGroupEntity) groupService.findByIdWithAttrs(id, "users");
		}
		return entity;
	}
}
