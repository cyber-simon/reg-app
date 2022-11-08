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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class ProjectAdminEditProjectAdminsBean implements Serializable {

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
	private List<ProjectIdentityAdminEntity> adminList;
	private List<ProjectServiceEntity> serviceList;
	private LazyDataModel<UserEntity> allUserList;
    
	private Long projectId;

	private ProjectIdentityAdminEntity adminIdentity;
	private ProjectAdminType selectedAdminType;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(projectId);
			selectedAdminType = ProjectAdminType.READ_WRITE;
		}
		
		for (ProjectIdentityAdminEntity a : getAdminList()) {
			if (a.getIdentity().getId().equals(session.getIdentityId())) {
				adminIdentity = a;
				break;
			}
		}
		
		if (adminIdentity == null) {
			throw new NotAuthorizedException("Nicht autorisiert");
		}
		else {
			if (! (ProjectAdminType.ADMIN.equals(adminIdentity.getType()) || ProjectAdminType.OWNER.equals(adminIdentity.getType()))) {
				throw new NotAuthorizedException("Nicht autorisiert");
			}
		}
	}
	
	public void addAdmin(UserEntity user) {
		projectService.addAdminToProject(entity, user.getIdentity(), getSelectedAdminType(), "idty-" + session.getIdentityId());
		adminList = null;
	}
	
	public void removeAdmin(ProjectIdentityAdminEntity pia) {
		projectService.removeAdminFromProject(pia, "idty-" + session.getIdentityId());
		adminList = null;
	}
	
	public String cancel() {
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
			Map<String, Object> filterMap = new HashMap<String, Object>();
			filterMap.put("userStatus", UserStatus.ACTIVE);
			allUserList = new GenericLazyDataModelImpl<UserEntity, UserService>(userService, filterMap);
		}
		return allUserList;
	}

	public ProjectAdminType getSelectedAdminType() {
		return selectedAdminType;
	}

	public void setSelectedAdminType(ProjectAdminType selectedAdminType) {
		this.selectedAdminType = selectedAdminType;
	}
	
	public ProjectAdminType[] getAdminTypes() {
		return ProjectAdminType.values();
	}
}
