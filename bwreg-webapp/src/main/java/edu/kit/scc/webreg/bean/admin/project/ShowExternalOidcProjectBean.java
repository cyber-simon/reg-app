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
import java.util.List;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity;
import edu.kit.scc.webreg.entity.project.ExternalOidcProjectEntity_;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.service.project.ExternalOidcProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;

@Named
@ViewScoped
public class ShowExternalOidcProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ExternalOidcProjectService service;

	@Inject
	private ProjectService projectService;

	private ExternalOidcProjectEntity entity;
	private List<ProjectMembershipEntity> memberList;
	private List<ProjectIdentityAdminEntity> adminList;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ExternalOidcProjectEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, ExternalOidcProjectEntity_.projectServices);
		}

		return entity;
	}

	public void setEntity(ExternalOidcProjectEntity entity) {
		this.entity = entity;
	}

	public List<ProjectMembershipEntity> getMemberList() {
		if (memberList == null) {
			memberList = projectService.findMembersForProject(getEntity());
		}
		return memberList;
	}

	public List<ProjectIdentityAdminEntity> getAdminList() {
		if (adminList == null) {
			adminList = projectService.findAdminsForProject(getEntity());
		}
		return adminList;
	}
}
