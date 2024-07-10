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
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class UserEditLocalProjectAdminsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;

	@Inject
	private LocalProjectService service;

	@Inject
	private UserService userService;

	@Inject
	private ProjectService projectService;

	@Inject
	private FacesMessageGenerator messageGenerator;

	private LocalProjectEntity entity;
	private Set<IdentityEntity> memberList;
	private Set<IdentityEntity> effectiveMemberList;
	private List<ProjectIdentityAdminEntity> adminList;
	private List<ProjectServiceEntity> serviceList;
	private LazyDataModel<UserEntity> allUserList;

	private Long projectId;

	private ProjectIdentityAdminEntity adminIdentity;

	private Boolean savePossible = false;

	private ProjectAdminType selectedAdminType;

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
			throw new NotAuthorizedException("Not authorized");
		} else {
			if (adminIdentity.getType().equals(ProjectAdminType.READ) || adminIdentity.getType().equals(ProjectAdminType.READ_WRITE)) {
				throw new NotAuthorizedException("Not authorized. You need Admin or Owner rights.");
			}
		}
	}

	public void addAdmin(IdentityEntity i) {
		if (getSelectedAdminType().equals(ProjectAdminType.OWNER) && (! adminIdentity.getType().equals(ProjectAdminType.OWNER))) {
			messageGenerator.addResolvedErrorMessage("project.edit_admins.cant_add_owner_as_admin");
			return;
		}

		projectService.addAdminToProject(entity, i, getSelectedAdminType(), "idty-" + session.getIdentityId());
		adminList = null;
	}

	public void removeAdmin(ProjectIdentityAdminEntity pia) {
		if (ProjectAdminType.OWNER.equals(pia.getType())) {
			// Only OWNERs can remove OWNERs
			if (! adminIdentity.getType().equals(ProjectAdminType.OWNER)) {
				messageGenerator.addResolvedErrorMessage("project.edit_admins.cant_remove_owner_as_admin");
				return;
			}

			if (getAdminList().stream().filter(a -> ProjectAdminType.OWNER.equals(a.getType())).count() <= 1L) {
				messageGenerator.addResolvedErrorMessage("project.edit_admins.cant_remove_last_owner");
				return;
			}
		}
		projectService.removeAdminFromProject(pia, "idty-" + session.getIdentityId());
		adminList = null;
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
			List<ProjectMembershipEntity> tempMemberList = projectService.findMembersForProject(getEntity());
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

	public ProjectAdminType getSelectedAdminType() {
		return selectedAdminType;
	}

	public void setSelectedAdminType(ProjectAdminType selectedAdminType) {
		this.selectedAdminType = selectedAdminType;
	}

	public ProjectAdminType[] getAdminTypes() {
		if (adminIdentity.getType().equals(ProjectAdminType.OWNER))
			return new ProjectAdminType[] { ProjectAdminType.OWNER, ProjectAdminType.ADMIN, ProjectAdminType.READ_WRITE, ProjectAdminType.READ };
		else if (adminIdentity.getType().equals(ProjectAdminType.ADMIN))
			return new ProjectAdminType[] { ProjectAdminType.ADMIN, ProjectAdminType.READ_WRITE, ProjectAdminType.READ };
		else
			return new ProjectAdminType[] { };
	}
}
