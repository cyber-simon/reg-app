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

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class AddServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceService serviceService;
	
	private ServiceEntity entity;
	
	public void preRenderView(ComponentSystemEvent ev) {
		entity = serviceService.createNew();
	}

	public String save() {
		serviceService.save(entity);
		return ViewIds.LIST_SERVICES;
	}
	
	public ServiceEntity getEntity() {
		return entity;
	}

	public void setEntity(ServiceEntity entity) {
		this.entity = entity;
	}
	
	
}
