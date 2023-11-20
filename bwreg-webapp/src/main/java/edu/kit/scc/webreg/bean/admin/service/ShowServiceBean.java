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
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.BBCodeConverter;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class ShowServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceService serviceService;

	@Inject
	private RegisterUserService registerUserService;
	
	@Inject
	private BBCodeConverter bbCodeConverter;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private ServiceEntity entity;
	
	private String serviceDescBB;

	private String deregisterTextBB;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = serviceService.findByIdWithServiceProps(id);
			serviceDescBB = bbCodeConverter.convert(entity.getDescription());
			deregisterTextBB = bbCodeConverter.convert(entity.getDeregisterText());
		}
	}
	
	public String startRecon(Boolean fullRecon, Boolean withGroups, Boolean onlyActive) {
		registerUserService.completeReconciliation(entity, fullRecon, withGroups, onlyActive, "identity-" + sessionManager.getIdentityId());
		messageGenerator.addInfoMessage("Job gestartet", "Bitte Log output beachten!");
		
		return ViewIds.SHOW_SERVICE + "?faces-redirect=true&id=" + entity.getId();
	}
	
	public ServiceEntity getEntity() {
		return entity;
	}

	public void setEntity(ServiceEntity entity) {
		this.entity = entity;
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

	public String getDeregisterTextBB() {
		return deregisterTextBB;
	}
}
