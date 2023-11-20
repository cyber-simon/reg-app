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
package edu.kit.scc.webreg.bean.project;

import java.io.Serializable;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class ProjectAdminAddProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;
	
	@Inject
	private LocalProjectService service;
	
	@Inject
	private ProjectService projectService;
	
	private LocalProjectEntity entity;

	private List<ProjectEntity> parentProjectList;
	private ProjectEntity selectedParentProject;
	
	public void preRenderView(ComponentSystemEvent ev) {
	}
	
	public String save() {
		entity.setParentProject(selectedParentProject);
		entity = service.save(entity, session.getIdentityId());
		
		return ViewIds.PROJECT_ADMIN_INDEX + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.PROJECT_ADMIN_INDEX + "&faces-redirect=true";
	}

	public LocalProjectEntity getEntity() {
		if (entity == null) { 
			entity = service.createNew();
			entity.setSubProjectsAllowed(Boolean.FALSE);
		}
		
		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}

	public List<ProjectEntity> getParentProjectList() {
		if (parentProjectList == null) {
			parentProjectList = projectService.findAllByAttr("subProjectsAllowed", Boolean.TRUE);
		}
		return parentProjectList;
	}

	public ProjectEntity getSelectedParentProject() {
		return selectedParentProject;
	}

	public void setSelectedParentProject(ProjectEntity selectedParentProject) {
		this.selectedParentProject = selectedParentProject;
	}
}
