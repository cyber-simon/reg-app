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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.JobClassEntity;
import edu.kit.scc.webreg.service.JobClassService;
import edu.kit.scc.webreg.util.ViewIds;

@Named("addJobClassBean")
@RequestScoped
public class AddJobClassBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private JobClassService service;

	private JobClassEntity entity;
	
	@PostConstruct
	public void init() {
		entity = service.createNew();
	}
	
	public String save() {
		service.save(entity);
		return ViewIds.LIST_JOBCLASS + "?faces-redirect=true";
	}

	public JobClassEntity getEntity() {
		return entity;
	}

	public void setEntity(JobClassEntity entity) {
		this.entity = entity;
	}
	
}
