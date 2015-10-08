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
package edu.kit.scc.webreg.bean.admin.group;

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;

@ManagedBean
@ViewScoped
public class GroupAdminListGroupsBean implements Serializable {

 	private static final long serialVersionUID = 1L;

	@Inject
    private ServiceGroupFlagService groupFlagService;
    
	@Inject
	private ServiceService serviceService;
	
    @Inject
    private AuthorizationBean authBean;

    private ServiceEntity serviceEntity;
    
    private List<ServiceGroupFlagEntity> groupFlagList;
    
    private Long serviceId;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) 
			serviceEntity = serviceService.findById(serviceId);
	
		if (! authBean.isUserServiceGroupAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
		
		if (groupFlagList == null)
			groupFlagList = groupFlagService.findLocalGroupsForService(serviceEntity);
	}

	public List<ServiceGroupFlagEntity> getGroupFlagList() {
		return groupFlagList;
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
