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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.service.ScriptService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.oidc.OidcClientConfigurationService;
import edu.kit.scc.webreg.service.oidc.ServiceOidcClientService;

@ManagedBean
@ViewScoped
public class ShowOidcClientConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcClientConfigurationService service;

	@Inject
	private ServiceOidcClientService serviceOidcClientService;
	
	@Inject
	private ScriptService scriptService;
	
	@Inject
	private ServiceService serviceService;
	
	private OidcClientConfigurationEntity entity;
	private List<ServiceOidcClientEntity> serviceOidcClientList;
	private List<ScriptEntity> scriptList;
	private List<ServiceEntity> serviceList;
	
	private Long id;

	private String newKey;
	private String newValue;

	private ServiceOidcClientEntity newSoce;
	
	public void preRenderView(ComponentSystemEvent ev) {
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
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, "genericStore");
		}
		return entity;
	}

	public void setEntity(OidcClientConfigurationEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void addSoce() {
		serviceOidcClientService.save(newSoce);
		newSoce = null;
		serviceOidcClientList = null;
	}
	
	public void removeSoce(ServiceOidcClientEntity oldSoce) {
		serviceOidcClientService.delete(oldSoce);
		newSoce = oldSoce;
		serviceOidcClientList = null;
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

	public List<ServiceOidcClientEntity> getServiceOidcClientList() {
		if (serviceOidcClientList == null)
			serviceOidcClientList = serviceOidcClientService.findByClientConfig(getEntity());
		return serviceOidcClientList;
	}

	public ServiceOidcClientEntity getNewSoce() {
		if (newSoce == null) {
			newSoce = new ServiceOidcClientEntity();
			newSoce.setClientConfig(getEntity());
		}
		return newSoce;
	}

	public void setNewSoce(ServiceOidcClientEntity newSoce) {
		this.newSoce = newSoce;
	}

	public List<ScriptEntity> getScriptList() {
		if (scriptList == null)
			scriptList = scriptService.findAll();
		return scriptList;
	}

	public List<ServiceEntity> getServiceList() {
		if (serviceList == null)
			serviceList = serviceService.findAll();
		return serviceList;
	}
}
