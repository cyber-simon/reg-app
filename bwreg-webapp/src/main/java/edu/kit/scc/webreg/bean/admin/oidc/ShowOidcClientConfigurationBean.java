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

import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity_;
import edu.kit.scc.webreg.entity.oidc.OidcRedirectUrlEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.service.BusinessRulePackageService;
import edu.kit.scc.webreg.service.ScriptService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.oidc.OidcClientConfigurationService;
import edu.kit.scc.webreg.service.oidc.OidcRedirectUrlService;
import edu.kit.scc.webreg.service.oidc.ServiceOidcClientService;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ShowOidcClientConfigurationBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private OidcClientConfigurationService service;

	@Inject
	private OidcRedirectUrlService redirectUrlService;

	@Inject
	private ServiceOidcClientService serviceOidcClientService;

	@Inject
	private ScriptService scriptService;

	@Inject
	private ServiceService serviceService;

	@Inject
	private BusinessRulePackageService rulePackageService;

	private OidcClientConfigurationEntity entity;
	private List<ServiceOidcClientEntity> serviceOidcClientList;
	private List<ScriptEntity> scriptList;
	private List<ServiceEntity> serviceList;
	private List<BusinessRulePackageEntity> rulePackageList;

	private Long id;

	private String newKey;
	private String newValue;
	private String newRedirect;

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

	public void addRedirect() {
		OidcRedirectUrlEntity redirectUrl = redirectUrlService.createNew();
		redirectUrl.setUrl(newRedirect);
		redirectUrl.setClient(getEntity());
		redirectUrlService.save(redirectUrl);
		entity = null;
		newRedirect = "";
	}

	public void removeRedirect(String redirect) {
		newRedirect = redirect;
		redirectUrlService.deleteUrl(redirect, getEntity());
		entity = null;
	}

	public OidcClientConfigurationEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, OidcClientConfigurationEntity_.genericStore,
					OidcClientConfigurationEntity_.redirects);
		}
		return entity;
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
		if (serviceOidcClientList == null) {
			serviceOidcClientList = serviceOidcClientService.findByClientConfig(getEntity());
		}
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

	public List<BusinessRulePackageEntity> getRulePackageList() {
		if (rulePackageList == null)
			rulePackageList = rulePackageService.findAll();
		return rulePackageList;
	}

	public String getNewRedirect() {
		return newRedirect;
	}

	public void setNewRedirect(String newRedirect) {
		this.newRedirect = newRedirect;
	}
}
