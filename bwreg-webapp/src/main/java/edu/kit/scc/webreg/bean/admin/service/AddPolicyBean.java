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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.PolicyService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class AddPolicyBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceService serviceService;

	@Inject
	private PolicyService policyService;

	private PolicyEntity entity;
	
	private ServiceEntity service;
	
	private Long serviceId;

	public void preRenderView(ComponentSystemEvent ev) {
		service = serviceService.findById(serviceId);
		entity = policyService.createNew();
	}
	
	public String save() {
		entity.setSevice(service);
		policyService.save(entity);
		return ViewIds.SHOW_SERVICE + "?faces-redirect=true&id=" + service.getId();
	}
	
	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public PolicyEntity getEntity() {
		return entity;
	}

	public void setEntity(PolicyEntity entity) {
		this.entity = entity;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}
}
