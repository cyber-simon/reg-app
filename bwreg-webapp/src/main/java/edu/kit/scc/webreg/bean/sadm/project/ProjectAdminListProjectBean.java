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
package edu.kit.scc.webreg.bean.sadm.project;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.project.ProjectService;

@ManagedBean
@ViewScoped
public class ProjectAdminListProjectBean implements Serializable {

 	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceService serviceService;
	
	@Inject
	private ProjectService projectService;
	
    @Inject
    private AuthorizationBean authBean;

    private ServiceEntity serviceEntity;
    
    private Long serviceId;
    
    private List<ProjectEntity> projectList;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) {
			serviceEntity = serviceService.findById(serviceId);
		}
	
		if (! authBean.isUserServiceProjectAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public List<ProjectEntity> getProjectList() {
		if (projectList == null)
			projectList = projectService.findByService(serviceEntity);
		return projectList;
	}
	
	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public void setServiceEntity(ServiceEntity serviceEntity) {
		this.serviceEntity = serviceEntity;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
}
