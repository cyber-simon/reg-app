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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectPolicyType;
import edu.kit.scc.webreg.service.PolicyService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
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

	private String policyType;
	
	public void preRenderView(ComponentSystemEvent ev) {	
	}
	
	public String save() {
		if ("project_policy".equals(policyType)) {
			getEntity().setProjectPolicy(service);
		}
		else {
			getEntity().setProjectPolicyType(null);
			getEntity().setSevice(service);
		}
		policyService.save(getEntity());
		return ViewIds.SHOW_SERVICE + "?faces-redirect=true&id=" + service.getId();
	}
	
	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public PolicyEntity getEntity() {
		if (entity == null) {
			entity = policyService.createNew();
		}
		return entity;
	}

	public void setEntity(PolicyEntity entity) {
		this.entity = entity;
	}

	public ServiceEntity getService() {
		if (service == null) {
			service = serviceService.fetch(serviceId);
		}
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	public String getPolicyType() {
		return policyType;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
	}
	
	public ProjectPolicyType[] getProjectPolicyTypes() {
		return ProjectPolicyType.values();
	}	
}
