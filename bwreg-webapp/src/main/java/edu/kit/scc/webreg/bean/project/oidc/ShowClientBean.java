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
package edu.kit.scc.webreg.bean.project.oidc;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.ProjectOidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ProjectOidcClientConfigurationEntity_;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.oidc.ProjectOidcClientConfigurationService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("project.oidc.ShowClientBean")
@ViewScoped
public class ShowClientBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private ProjectService projectService;

	@Inject
	private ProjectOidcClientConfigurationService service;

	@Inject
	private IdentityService identityService;

	private Long clientId;

	private IdentityEntity identity;
	private ProjectEntity project;
	private List<ProjectIdentityAdminEntity> adminList;
	private ProjectIdentityAdminEntity adminIdentity;

	private ProjectOidcClientConfigurationEntity entity;

	public void preRenderView(ComponentSystemEvent ev) {
		for (ProjectIdentityAdminEntity a : getAdminList()) {
			if (a.getIdentity().getId().equals(session.getIdentityId())) {
				adminIdentity = a;
				break;
			}
		}

		if (adminIdentity == null) {
			throw new NotAuthorizedException("Nicht autorisiert");
		} else {
			if (!(ProjectAdminType.ADMIN.equals(adminIdentity.getType())
					|| ProjectAdminType.OWNER.equals(adminIdentity.getType()))) {
				throw new NotAuthorizedException("Nicht autorisiert");
			}
		}

		if (!getProject().getApproved()) {
			throw new NotAuthorizedException("Project is not approved for self managed oidc credentials");
		}
	}

	public String cancelDelete() {
		return "show-client.xhtml?id=" + getEntity().getId() + "&faces-redirect=true";
	}

	public String commitDelete() {
		service.delete(entity);
		return "list-clients.xhtml?id=" + getProject().getId() + "&faces-redirect=true";
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.fetch(session.getIdentityId());
		}
		return identity;
	}

	public ProjectEntity getProject() {
		if (project == null)
			project = projectService.fetch(getEntity().getProject().getId());
		return project;
	}

	public List<ProjectIdentityAdminEntity> getAdminList() {
		if (adminList == null) {
			adminList = projectService.findAdminsForProject(getProject());
		}
		return adminList;
	}

	public ProjectOidcClientConfigurationEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(clientId, ProjectOidcClientConfigurationEntity_.redirects);
		}
		return entity;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
}
