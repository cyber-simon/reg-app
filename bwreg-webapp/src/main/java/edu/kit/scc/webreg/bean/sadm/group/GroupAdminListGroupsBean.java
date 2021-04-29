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
package edu.kit.scc.webreg.bean.sadm.group;

import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceGroupFlagService;
import edu.kit.scc.webreg.service.ServiceService;

@Named
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
		if (serviceEntity == null) {
			serviceEntity = serviceService.findById(serviceId);
		}
	
		if (! authBean.isUserServiceGroupAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public List<ServiceGroupFlagEntity> getGroupFlagList() {
		if (groupFlagList == null)
			groupFlagList = groupFlagService.findLocalGroupsForService(serviceEntity);
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
