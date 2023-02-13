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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@ViewScoped
public class ProjectAdminEditProjectMembersBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private LocalProjectService service;

	@Inject
	private UserService userService;

	@Inject
	private ProjectService projectService;

	private LocalProjectEntity entity;
	private Set<IdentityEntity> memberList;
	private Set<IdentityEntity> effectiveMemberList;
	private List<ProjectIdentityAdminEntity> adminList;
	private List<ProjectServiceEntity> serviceList;
	private LazyDataModel<UserEntity> allUserList;

	private Long projectId;

	private ProjectIdentityAdminEntity adminIdentity;

	private Boolean savePossible = false;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(projectId);
		}

		for (ProjectIdentityAdminEntity a : getAdminList()) {
			if (a.getIdentity().getId().equals(session.getIdentityId())) {
				adminIdentity = a;
				break;
			}
		}

		if (adminIdentity == null) {
			throw new NotAuthorizedException("Nicht autorisiert");
		} else {
			if (adminIdentity.getType().equals(ProjectAdminType.READ)) {
				throw new NotAuthorizedException("Nicht autorisiert");
			}
		}
	}

	public void addMember(UserEntity user) {
		savePossible = true;
		getMemberList().add(user.getIdentity());
	}

	public void removeMember(IdentityEntity identity) {
		savePossible = true;
		getMemberList().remove(identity);
	}

	public String save() {
		projectService.updateProjectMemberList(entity, memberList, "idty-" + session.getIdentityId());
		return "show-project.xhtml?faces-redirect=true&projectId=" + entity.getId();
	}

	public String cancel() {
		savePossible = false;
		return "show-project.xhtml?faces-redirect=true&projectId=" + entity.getId();
	}

	public LocalProjectEntity getEntity() {
		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Set<IdentityEntity> getMemberList() {
		if (memberList == null) {
			List<ProjectMembershipEntity> tempMemberList = projectService.findMembersForProject(entity);
			memberList = new HashSet<IdentityEntity>();
			for (ProjectMembershipEntity pme : tempMemberList) {
				memberList.add(pme.getIdentity());
			}
		}
		return memberList;
	}

	public List<ProjectIdentityAdminEntity> getAdminList() {
		if (adminList == null) {
			adminList = projectService.findAdminsForProject(entity);
		}
		return adminList;
	}

	public List<ProjectServiceEntity> getServiceList() {
		if (serviceList == null) {
			serviceList = projectService.findServicesForProject(entity);
		}
		return serviceList;
	}

	public LazyDataModel<UserEntity> getAllUserList() {
		if (allUserList == null) {
			allUserList = new GenericLazyDataModelImpl<UserEntity, UserService>(userService,
					equal("userStatus", UserStatus.ACTIVE));
		}
		return allUserList;
	}

	public Boolean getSavePossible() {
		return savePossible;
	}

	public ProjectIdentityAdminEntity getAdminIdentity() {
		return adminIdentity;
	}

	public Set<IdentityEntity> getEffectiveMemberList() {
		if (effectiveMemberList == null) {
			List<ProjectMembershipEntity> tempMemberList = projectService.findMembersForProject(entity, true);
			effectiveMemberList = new HashSet<IdentityEntity>();
			for (ProjectMembershipEntity pme : tempMemberList) {
				effectiveMemberList.add(pme.getIdentity());
			}
		}
		return effectiveMemberList;
	}
}
