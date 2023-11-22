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
package edu.kit.scc.webreg.bean.admin.saml;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.service.FederationService;

@Named("addFederationBean")
@RequestScoped
public class AddFederationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FederationService service;

	private FederationEntity entity;
	
	@PostConstruct
	public void init() {
		entity = service.createNew();
		entity.setFetchIdps(Boolean.TRUE);
		entity.setFetchSps(Boolean.FALSE);
		entity.setFetchAAs(Boolean.FALSE);
	}

	public String save() {
		entity = service.save(entity);
		return "show-federation.xhtml?faces-redirect=true&id=" + entity.getId();
	}
	
	public FederationEntity getEntity() {
		return entity;
	}

	public void setEntity(FederationEntity entity) {
		this.entity = entity;
	}
}
