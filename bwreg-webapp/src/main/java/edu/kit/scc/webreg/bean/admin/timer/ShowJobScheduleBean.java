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
package edu.kit.scc.webreg.bean.admin.timer;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.JobScheduleEntity;
import edu.kit.scc.webreg.service.JobScheduleService;

@Named("showJobScheduleBean")
@RequestScoped
public class ShowJobScheduleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private JobScheduleService service;

	private JobScheduleEntity entity;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.fetch(id);
	}
	
	public JobScheduleEntity getEntity() {
		return entity;
	}

	public void setEntity(JobScheduleEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
