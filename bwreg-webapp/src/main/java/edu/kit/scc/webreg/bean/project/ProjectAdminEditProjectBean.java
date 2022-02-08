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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class ProjectAdminEditProjectBean implements Serializable {

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
	
	private Long projectId;
	
	public void preRenderView(ComponentSystemEvent ev) {
	}
	
	public String save() {
		entity.setParentProject(selectedParentProject);
		entity = service.save(entity);
		
		return ViewIds.PROJECT_ADMIN_INDEX + "&faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.PROJECT_ADMIN_INDEX + "&faces-redirect=true";
	}

	public LocalProjectEntity getEntity() {
		if (entity == null) { 
			entity = service.findById(projectId);
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

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}
}
