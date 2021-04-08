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
package edu.kit.scc.webreg.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.servlet.http.Cookie;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.SamlSpMetadataService;
import edu.kit.scc.webreg.service.oidc.OidcClientConfigurationService;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;
import edu.kit.scc.webreg.service.oidc.OidcRpConfigurationService;
import edu.kit.scc.webreg.service.saml.FederationSingletonBean;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.CookieHelper;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class DiscoveryLoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FederationSingletonBean federationBean;

	@Inject
	private SamlIdpMetadataService idpService;
	
	@Inject
	private SamlSpConfigurationService spService;
	
	@Inject
	private OidcRpConfigurationService oidcRpService;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private SamlIdpConfigurationService idpConfigService;

	@Inject
	private SamlSpMetadataService spMetadataService;
	
	@Inject
	private OidcOpConfigurationService oidcOpConfigService;
	
	@Inject
	private OidcClientConfigurationService oidcClientConfigService;
	
	@Inject
	private CookieHelper cookieHelper;
	
	private List<FederationEntity> federationList;
	private List<SamlIdpMetadataEntity> idpList;
	private FederationEntity selectedFederation;
	private SamlIdpMetadataEntity selectedIdp;

	private Boolean storeIdpSelection;
	
	private List<OidcRpConfigurationEntity> oidcRpList;
	private OidcRpConfigurationEntity selectedOidcRp;
	
	private String filter;
	
	/*
	 * Login came from SAML SP.
	 * spMetadata holds metadata of requester
	 * idpConfig is the IDP on reg-app side, which was requested
	 */
	private SamlSpMetadataEntity spMetadata;
	private SamlIdpConfigurationEntity idpConfig;
	
	/*
	 * Login came from OIDC RP.
	 * 
	 */
	private OidcOpConfigurationEntity opConfig;
	private OidcClientConfigurationEntity clientConfig;
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (sessionManager.getOriginalIdpEntityId() != null) {
			SamlIdpMetadataEntity idp = idpService.findByEntityId(sessionManager.getOriginalIdpEntityId());
			if (idp != null) {
				selectedIdp = idp;
				login();
				return;
			}
		}
		
		if (sessionManager.getOriginalFederationShortName() != null) {
			FederationEntity f = federationBean.getFederationList().stream()
					.filter(federation -> sessionManager.getOriginalFederationShortName().equals(federation.getShortName()))
					.findFirst()
					.orElse(null);
			if (f != null) {
				selectedFederation = f;
			}
		}

		if (! initialized) {
			if (appConfig.getConfigValue("preselect_store_idp_select") != null &&
					appConfig.getConfigValue("preselect_store_idp_select").equalsIgnoreCase("true")) {
				storeIdpSelection = true;
			}
			else {
				storeIdpSelection = false;
			}

			federationList = federationBean.getFederationList();
			if (federationList == null || federationList.size() == 0) {
				messageGenerator.addErrorMessage("Das SAML Subsystem ist noch nicht konfiguriert");
				return;
			}
			updateIdpList();
			Cookie idpCookie = cookieHelper.getCookie("preselect_idp");
			if (idpCookie != null) {
				Long idpId = Long.parseLong(idpCookie.getValue());
				if (idpId != null) {
					SamlIdpMetadataEntity idp = idpService.findById(idpId);
					if (idp != null) {
						selectedIdp = idp;
						storeIdpSelection = true;
					}										
				}
			}
			initialized = true;
		}
	}
	
	public void login() {
		if (selectedIdp != null) {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String hostname = externalContext.getRequestServerName();
			SamlSpConfigurationEntity spConfig;
			spConfig = spService.findByHostname(hostname);
			
			if (spConfig == null) {
				messageGenerator.addErrorMessage("Es ist keine Host Konfiguration vorhanden", 
								"Betroffener Host: " + hostname);
				return;
			}
			
			sessionManager.setSpId(spConfig.getId());
			sessionManager.setIdpId(selectedIdp.getId());
			if (storeIdpSelection != null && storeIdpSelection) {
				cookieHelper.setCookie("preselect_idp", selectedIdp.getId().toString(), 356 * 24 * 3600);
			}
			else {
				cookieHelper.setCookie("preselect_idp", "", 0);
			}
			try {
				externalContext.redirect("/Shibboleth.sso/Login");
			} catch (IOException e) {
				messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", 
								e.toString());
			}
		}
		else {
				messageGenerator.addWarningMessage("Keine Auswahl getroffen", 
							"Bitte wählen Sie Ihre Heimatorganisation");
		}
	}

	public void oidcLogin() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		
		if (selectedOidcRp != null) {
			sessionManager.setOidcRelyingPartyId(selectedOidcRp.getId());
			try {
				externalContext.redirect("/rpoidc/login");
			} catch (IOException e) {
				messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", 
								e.toString());
			}
		}
		else {
				messageGenerator.addWarningMessage("Keine Auswahl getroffen", 
							"Bitte wählen Sie Ihre Heimatorganisation");
		}
	}
	
	public void updateIdpList() {
		if (selectedFederation == null) {
			if (sessionManager.getOidcAuthnOpConfigId() != null && 
					sessionManager.getOidcAuthnClientConfigId() != null) {
				/*
				 * reg-app login called via OIDC relying party
				 */
				opConfig = oidcOpConfigService.findById(sessionManager.getOidcAuthnOpConfigId());
				clientConfig = oidcClientConfigService.findById(sessionManager.getOidcAuthnClientConfigId());
				idpList = federationBean.getAllIdpList();
			}
			else if (sessionManager.getAuthnRequestIdpConfigId() != null && 
					sessionManager.getAuthnRequestSpMetadataId() != null) {
				/*
				 * reg-app login called via SAML service provider
				 */
				idpConfig = idpConfigService.findById(sessionManager.getAuthnRequestIdpConfigId());
				spMetadata = spMetadataService.findById(sessionManager.getAuthnRequestSpMetadataId());
				List<ServiceSamlSpEntity> serviceSamlList = idpConfigService.findBySamlSpAndIdp(idpConfig, spMetadata);
				idpList = new ArrayList<SamlIdpMetadataEntity>();
				
				for (ServiceSamlSpEntity serviceSaml : serviceSamlList) {
					if (serviceSaml.getScript() != null) {
						idpList.addAll(federationBean.getFilteredIdpList(serviceSaml.getScript()));
					}
				}
			}
			else {
				/*
				 * reg-app login directly called
				 */
				idpList = federationBean.getAllIdpList();
			}
		}
		else {
			idpList = federationBean.getIdpList(selectedFederation);
		}
	}
	
	public List<FederationEntity> getFederationList() {
		return federationList;
	}

	public FederationEntity getSelectedFederation() {
		return selectedFederation;
	}

	public void setSelectedFederation(FederationEntity selectedFederation) {
		this.selectedFederation = selectedFederation;
		updateIdpList();
	}

	public SamlIdpMetadataEntity getSelectedIdp() {
		return selectedIdp;
	}

	public void setSelectedIdp(SamlIdpMetadataEntity selectedIdp) {
		this.selectedIdp = selectedIdp;
	}

	public List<SamlIdpMetadataEntity> getIdpList() {
		if (filter == null)
			return idpList;
		
		List<SamlIdpMetadataEntity> filteredList = new ArrayList<SamlIdpMetadataEntity>();
		
		for (SamlIdpMetadataEntity idp : idpList) {
			if (idp.getOrgName() != null &&
					Pattern.compile(
					Pattern.quote(filter), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
					.matcher(idp.getOrgName()).find()) {
				filteredList.add(idp);
			}
			else if (idp.getDisplayName() != null &&
					Pattern.compile(
					Pattern.quote(filter), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
					.matcher(idp.getDisplayName()).find()) {
				filteredList.add(idp);
			}
		}
		
		if (filteredList.size() == 1) {
			selectedIdp = filteredList.get(0);
		}

		return filteredList;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public List<OidcRpConfigurationEntity> getOidcRpList() {
		if (oidcRpList == null) {
			oidcRpList = oidcRpService.findAll();
		}
		return oidcRpList;
	}

	public OidcRpConfigurationEntity getSelectedOidcRp() {
		return selectedOidcRp;
	}

	public void setSelectedOidcRp(OidcRpConfigurationEntity selectedOidcRp) {
		this.selectedOidcRp = selectedOidcRp;
	}

	public ApplicationConfig getAppConfig() {
		return appConfig;
	}

	public SamlSpMetadataEntity getSpMetadata() {
		return spMetadata;
	}

	public SamlIdpConfigurationEntity getIdpConfig() {
		return idpConfig;
	}

	public OidcOpConfigurationEntity getOpConfig() {
		return opConfig;
	}

	public OidcClientConfigurationEntity getClientConfig() {
		return clientConfig;
	}

	public Boolean getStoreIdpSelection() {
		return storeIdpSelection;
	}

	public void setStoreIdpSelection(Boolean storeIdpSelection) {
		this.storeIdpSelection = storeIdpSelection;
	}

}
