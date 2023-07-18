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

import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity_;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.project.AttributeSourceProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@ViewScoped
public class UserShowASProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private AttributeSourceProjectService service;

	@Inject
	private ProjectService projectService;

	private AttributeSourceProjectEntity entity;

	private List<ProjectMembershipEntity> memberList;
	private List<ProjectMembershipEntity> effectiveMemberList;
	private List<ProjectIdentityAdminEntity> adminList;
	private List<ProjectServiceEntity> serviceList;
	private List<ProjectServiceEntity> serviceFromParentsList;

	private ProjectIdentityAdminEntity adminIdentity;

	private Long id;

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
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AttributeSourceProjectEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, LocalProjectEntity_.projectServices);
		}

		return entity;
	}

	public void deleteMember(ProjectMembershipEntity pme) {
		projectService.removeProjectMember(pme, "idty-" + session.getIdentityId());
		memberList = null;
		effectiveMemberList = null;
	}

	public void setEntity(AttributeSourceProjectEntity entity) {
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

	public List<ProjectServiceEntity> getServiceList() {
		if (serviceList == null) {
			serviceList = projectService.findServicesForProject(getEntity());
		}
		return serviceList;
	}

	public ProjectIdentityAdminEntity getAdminIdentity() {
		return adminIdentity;
	}

	public List<ProjectServiceEntity> getServiceFromParentsList() {
		if (serviceFromParentsList == null) {
			serviceFromParentsList = projectService.findServicesFromParentsForProject(getEntity());
		}
		return serviceFromParentsList;
	}

	public List<ProjectMembershipEntity> getEffectiveMemberList() {
		if (effectiveMemberList == null) {
			effectiveMemberList = projectService.findMembersForProject(getEntity(), true);
		}
		return effectiveMemberList;
	}
}
