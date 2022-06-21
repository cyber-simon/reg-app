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
import java.util.List;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectService;

@Named
@ViewScoped
public class ConnectLocalProjectToServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private LocalProjectService service;

	@Inject
	private ServiceService serviceService;

	@Inject
	private ProjectService projectService;

	private LocalProjectEntity entity;

	private List<ServiceEntity> serviceList;
	private List<ServiceEntity> selectedServices;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
	}

	public String save() {
		
		return "show-local-project.xhtml?id=" + getEntity().getId();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalProjectEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, "projectServices");
		}

		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}

	public List<ServiceEntity> getServiceList() {
		if (serviceList == null) {
			serviceList = serviceService.findAllByAttr("projectCapable", Boolean.TRUE);
		}
		return serviceList;
	}

	public List<ServiceEntity> getSelectedServices() {
		return selectedServices;
	}

	public void setSelectedServices(List<ServiceEntity> selectedServices) {
		this.selectedServices = selectedServices;
	}
}
