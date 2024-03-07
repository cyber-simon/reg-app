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
package edu.kit.scc.webreg.bean.admin.project;

import java.io.Serializable;

import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("admin.editLocalProjectBean")
@ViewScoped
public class EditLocalProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private LocalProjectService service;
	
	private LocalProjectEntity entity;
	
	private Long projectId;

	public void preRenderView(ComponentSystemEvent ev) {
	}

	public String save() {
		entity = service.save(entity);
		return "show-local-project.xhtml?id=" + getEntity().getId() + "&faces-redirect=true"; 
	}
	
	public String cancel() {
		return "show-local-project.xhtml?id=" + getEntity().getId() + "&faces-redirect=true"; 
	}
	
	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public LocalProjectEntity getEntity() {
		if (entity == null) {
			entity = service.fetch(projectId);
		}
		return entity;
	}

}
