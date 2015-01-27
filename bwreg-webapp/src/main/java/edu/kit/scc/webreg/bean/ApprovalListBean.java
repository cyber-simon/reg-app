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
package edu.kit.scc.webreg.bean;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;

@Named("approvalListBean")
@RequestScoped
public class ApprovalListBean {

	private List<RegistryEntity> list;
    
    @Inject
    private RegistryService service;

    @Inject
    private ServiceService serviceService;
    
    private ServiceEntity serviceEntity;
    
    private Long serviceId;

	public void preRenderView(ComponentSystemEvent ev) {
		serviceEntity = serviceService.findById(serviceId);
		list = service.findByServiceAndStatus(serviceEntity, RegistryStatus.PENDING);
	}

    public List<RegistryEntity> getRoleEntityList() {
        return list;
    }

	public List<RegistryEntity> getList() {
		return list;
	}

	public void setList(List<RegistryEntity> list) {
		this.list = list;
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
