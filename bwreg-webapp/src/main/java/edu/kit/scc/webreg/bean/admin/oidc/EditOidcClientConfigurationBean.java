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
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationStatusType;
import edu.kit.scc.webreg.service.oidc.OidcClientConfigurationService;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;

@Named
@ViewScoped
public class EditOidcClientConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcClientConfigurationService service;
	
	@Inject
	private OidcOpConfigurationService opService;
	
	private OidcClientConfigurationEntity entity;
	
	private Long id;
	private List<OidcOpConfigurationEntity> opList;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(id);
		}
	}
	
	public String save() {
		service.save(entity);
		return "show-client-config.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public OidcClientConfigurationEntity getEntity() {
		return entity;
	}

	public void setEntity(OidcClientConfigurationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<OidcOpConfigurationEntity> getOpList() {
		if (opList == null)
			opList = opService.findAllByAttr("opStatus", OidcOpConfigurationStatusType.ACTIVE);
		return opList;
	}
}
