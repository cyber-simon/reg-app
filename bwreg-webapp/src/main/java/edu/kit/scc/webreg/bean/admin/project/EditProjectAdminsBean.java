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
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class EditProjectAdminsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;
	
	@Inject
	private ProjectService service;

    @Inject
    private UserService userService;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private ProjectEntity entity;
	private List<ProjectIdentityAdminEntity> adminList;
	private List<ProjectServiceEntity> serviceList;
	private LazyDataModel<UserEntity> allUserList;
    
	private Long projectId;

	private ProjectAdminType selectedAdminType;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(projectId);
			selectedAdminType = ProjectAdminType.READ_WRITE;
		}
	}
	
	public void addAdmin(UserEntity user) {
		service.addAdminToProject(entity, user.getIdentity(), getSelectedAdminType(), "idty-" + session.getIdentityId());
		adminList = null;
	}
	
	public void removeAdmin(ProjectIdentityAdminEntity pia) {
		service.removeAdminFromProject(pia, "idty-" + session.getIdentityId());
		adminList = null;
	}
	
	public String cancel() {
		return "show-project.xhtml?faces-redirect=true&projectId=" + entity.getId();
	}
	
	public ProjectEntity getEntity() {
		return entity;
	}

	public void setEntity(ProjectEntity entity) {
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
			adminList = service.findAdminsForProject(entity);
		}
		return adminList;
	}

	public List<ProjectServiceEntity> getServiceList() {
		if (serviceList == null) {
			serviceList = service.findServicesForProject(entity);
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
