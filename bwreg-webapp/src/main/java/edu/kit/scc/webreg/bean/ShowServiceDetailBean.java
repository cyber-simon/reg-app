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

import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.BBCodeConverter;

@Named("showServiceDetailBean")
@RequestScoped
public class ShowServiceDetailBean {

	@Inject
	private ServiceService serviceService;

	@Inject
	private BBCodeConverter bbCodeConverter;

	private ServiceEntity serviceEntity;
	
	private String serviceDescBB;

	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		serviceEntity = serviceService.findById(id);
		serviceDescBB = bbCodeConverter.convert(serviceEntity.getDescription());
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
}
