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

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceService;

@Named("checkServiceReqsBean")
@RequestScoped
public class CheckServiceReqsBean {

	@Inject
	private ServiceService serviceService;

	@Inject
	private AuthorizationBean authBean;
	
	private ServiceEntity serviceEntity;
	
	private String serviceDescBB;

	private Long id;
	
	private Boolean registered;
	
	public void preRenderView(ComponentSystemEvent ev) {
		serviceEntity = serviceService.fetch(id);
		registered = authBean.isUserInService(serviceEntity);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServiceDescBB() {
		return serviceDescBB;
	}

	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public void setServiceEntity(ServiceEntity serviceEntity) {
		this.serviceEntity = serviceEntity;
	}

	public Boolean getRegistered() {
		return registered;
	}
}
