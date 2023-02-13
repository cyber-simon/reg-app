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
package edu.kit.scc.webreg.bean.admin.oidc;

import java.io.Serializable;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;

@Named
@ViewScoped
public class EditOidcOpConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcOpConfigurationService service;
	
	private OidcOpConfigurationEntity entity;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(id);
		}
	}
	
	public String save() {
		service.save(entity);
		return "show-op-config.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public OidcOpConfigurationEntity getEntity() {
		return entity;
	}

	public void setEntity(OidcOpConfigurationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
