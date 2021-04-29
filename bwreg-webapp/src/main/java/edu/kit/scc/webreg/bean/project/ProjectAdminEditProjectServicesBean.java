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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.model.LazyDataModel;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class ProjectAdminEditProjectServicesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;
	
	@Inject
	private LocalProjectService service;

    @Inject
    private ServiceService serviceService;

	@Inject
	private ProjectService projectService;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private LocalProjectEntity entity;
	private List<ProjectIdentityAdminEntity> adminList;
	private Set<ServiceEntity> serviceList;
	private LazyDataModel<ServiceEntity> allServiceList;
    
	private Long projectId;

	private ProjectIdentityAdminEntity adminIdentity;

	private Boolean savePossible = false;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findById(projectId);
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
	
	public void addService(ServiceEntity service) {
		savePossible = true;
		getServiceList().add(service);
	}
	
	public void removeService(ServiceEntity service) {
		savePossible = true;
		getServiceList().remove(service);
	}
	
	public String save() {
		projectService.updateServices(entity, getServiceList(), "idty-" + session.getIdentityId());
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

	public List<ProjectIdentityAdminEntity> getAdminList() {
		if (adminList == null) {
			adminList = projectService.findAdminsForProject(entity);
		}
		return adminList;
	}

	public Set<ServiceEntity> getServiceList() {
		if (serviceList == null) {
			serviceList = new HashSet<ServiceEntity>();
			projectService.findServicesForProject(entity).forEach((ps) -> { serviceList.add(ps.getService()); } );
		}
		return serviceList;
	}

	public Boolean getSavePossible() {
		return savePossible;
	}

	public LazyDataModel<ServiceEntity> getAllServiceList() {
		if (allServiceList == null) {
			allServiceList = new GenericLazyDataModelImpl<ServiceEntity, ServiceService, Long>(serviceService);
		}
		return allServiceList;
	}
}
