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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.entity.JobScheduleEntity;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.service.JobScheduleService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class EditJobScheduleBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private JobScheduleService service;
	
	@Inject
	private JobClassService jobClassService;
	
	private JobScheduleEntity entity;
	
	private List<JobClassEntity> jobClassList;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(id);
			jobClassList = jobClassService.findAll();
		}
	}
	
	public String save() {
		service.save(entity);
		return ViewIds.SHOW_SCHEDULE + "?faces-redirect=true&id=" + entity.getId();
	}

	public String cancel() {
		return ViewIds.SHOW_SCHEDULE + "?faces-redirect=true&id=" + entity.getId();
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

	public List<JobClassEntity> getJobClassList() {
		return jobClassList;
	}

	public void setJobClassList(List<JobClassEntity> jobClassList) {
		this.jobClassList = jobClassList;
	}
}
