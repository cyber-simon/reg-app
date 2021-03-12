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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.service.oidc.OidcClientConfigurationService;

@ManagedBean
@ViewScoped
public class ShowOidcClientConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcClientConfigurationService service;

	private OidcClientConfigurationEntity entity;
	
	private Long id;

	private String newKey;
	private String newValue;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, "genericStore");
		}
	}
	
	public void addGenericStore() {
		getEntity().getGenericStore().put(newKey, newValue);
		entity = service.save(getEntity());
		newKey = "";
		newValue = "";
	}
	
	public void removeGenericStore(String key) {
		newKey = key;
		newValue = getEntity().getGenericStore().remove(key);
		entity = service.save(getEntity());
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

	public String getNewKey() {
		return newKey;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
}
