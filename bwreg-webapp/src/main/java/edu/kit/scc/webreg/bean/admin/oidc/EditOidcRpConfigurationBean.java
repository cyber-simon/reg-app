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
import java.util.Date;

import edu.kit.scc.webreg.entity.IconCacheEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.service.impl.IconCacheService;
import edu.kit.scc.webreg.service.oidc.OidcRpConfigurationService;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class EditOidcRpConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcRpConfigurationService service;
	
	@Inject
	private IconCacheService iconService;
	
	private OidcRpConfigurationEntity entity;
	
	private Long id;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.fetch(id);
		}
	}
	
	public String save() {
		if (entity.getIcon() == null) {
			IconCacheEntity icon = iconService.createNew();
			icon = iconService.save(icon);
			entity.setIcon(icon);
		}

		if (entity.getIconLarge() == null) {
			IconCacheEntity icon = iconService.createNew();
			icon = iconService.save(icon);
			entity.setIconLarge(icon);
		}

		if (entity.getLogoUrl() != null) {
			entity.getIconLarge().setUrl(entity.getLogoUrl());
			entity.getIconLarge().setValidUntil(new Date());
		}
		
		if (entity.getLogoSmallUrl() != null) {
			entity.getIcon().setUrl(entity.getLogoSmallUrl());
			entity.getIcon().setValidUntil(new Date());
		}
		
		iconService.save(entity.getIconLarge());
		iconService.save(entity.getIcon());
		service.save(entity);
		return "show-rp-config.xhtml?faces-redirect=true&id=" + entity.getId();
	}

	public OidcRpConfigurationEntity getEntity() {
		return entity;
	}

	public void setEntity(OidcRpConfigurationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
