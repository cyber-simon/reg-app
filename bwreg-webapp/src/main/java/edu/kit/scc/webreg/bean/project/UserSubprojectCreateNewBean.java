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

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class UserSubprojectCreateNewBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private LocalProjectService localProjectService;

	@Inject
	private ProjectService projectService;
	
	@Inject
	private IdentityService identityService;

	private IdentityEntity identity;
	private LocalProjectEntity entity;

	private Boolean selfMember;
	private Long parentId;
	private LocalProjectEntity parentProject;
	
	private ProjectIdentityAdminEntity adminIdentity;
	private List<ProjectIdentityAdminEntity> adminList;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! getParentProject().getSubProjectsAllowed()) {
			throw new NotAuthorizedException("Subpropjects are not allowed for this project");
		}
		
		for (ProjectIdentityAdminEntity a : getAdminList()) {
			if (a.getIdentity().getId().equals(session.getIdentityId())) {
				adminIdentity = a;
				break;
			}
		}

		if (adminIdentity == null) {
			throw new NotAuthorizedException("Not authorized");
		} else {
			if (! adminIdentity.getType().equals(ProjectAdminType.OWNER)) {
				throw new NotAuthorizedException("Not authorized. You need Owner rights on the parent project.");
			}
		}
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.fetch(session.getIdentityId());
		}
		return identity;
	}

	public String save() {
		entity.setParentProject(getParentProject());
		entity = localProjectService.save(entity, getIdentity().getId());
		if (getSelfMember()) {
			projectService.addProjectMember(entity, getIdentity(), "idty-" + getIdentity().getId());
		}
		return "show-local-project.xhtml?id=" + entity.getId() + "&faces-redirect=true";
	}

	public String cancel() {
		return "index.xhtml?faces-redirect=true";
	}

	public LocalProjectEntity getEntity() {
		if (entity == null) {
			entity = localProjectService.createNew();
		}
		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}

	public Boolean getSelfMember() {
		if (selfMember == null) {
			selfMember = Boolean.FALSE;
		}
		return selfMember;
	}

	public void setSelfMember(Boolean selfMember) {
		this.selfMember = selfMember;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public LocalProjectEntity getParentProject() {
		if (parentProject == null) {
			parentProject = localProjectService.fetch(getParentId());
		}
		return parentProject;
	}
	
	public List<ProjectIdentityAdminEntity> getAdminList() {
		if (adminList == null) {
			adminList = projectService.findAdminsForProject(getParentProject());
		}
		return adminList;
	}
}
