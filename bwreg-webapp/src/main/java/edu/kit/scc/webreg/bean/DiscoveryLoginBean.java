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

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.servlet.http.Cookie;

import org.primefaces.PrimeFaces;

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

@Named
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
	
	private List<Object> federationList;
	private List<Object> idpList;
	private Object selectedFederation;
	private Object selectedIdp;

	private Boolean storeIdpSelection;
	private Boolean preSelectedIdp;
	
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
			if (idpList == null) {
				idpList = new ArrayList<Object>();
			}
			
			preSelectedIdp = false;
			
			if (appConfig.getConfigValue("preselect_store_idp_select") != null &&
					appConfig.getConfigValue("preselect_store_idp_select").equalsIgnoreCase("true")) {
				storeIdpSelection = true;
			}
			else {
				storeIdpSelection = false;
			}

			getFederationList().addAll(federationBean.getFederationList());
			
			if (federationList == null || federationList.size() == 0) {
				messageGenerator.addErrorMessage("Das SAML Subsystem ist noch nicht konfiguriert");
				return;
			}

			updateIdpList();

			if (appConfig.getConfigValueOrDefault("show_oidc_login", "false").equalsIgnoreCase("true")) {
				idpList.addAll(oidcRpService.findAll());
			}
			
			Cookie idpCookie = cookieHelper.getCookie("preselect_idp");
			if (idpCookie != null) {
				String cookieValue = idpCookie.getValue();
				cookieValue = cookieValue.replaceAll("[^0-9]", "");

				Long idpId = Long.parseLong(cookieValue);
				SamlIdpMetadataEntity idp = idpService.findById(idpId);
				if (idp != null) {
					selectedIdp = idp;
					storeIdpSelection = true;
					preSelectedIdp = true;
					PrimeFaces.current().focus("quicklogin");
				}
				else {
					OidcRpConfigurationEntity op = oidcRpService.findById(idpId);
					if (op != null) {
						selectedIdp = op;
						storeIdpSelection = true;
						preSelectedIdp = true;
						PrimeFaces.current().focus("quicklogin");
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

			if (selectedIdp instanceof SamlIdpMetadataEntity) {
				SamlIdpMetadataEntity idp = (SamlIdpMetadataEntity) selectedIdp;
				SamlSpConfigurationEntity spConfig;
				spConfig = spService.findByHostname(hostname);
				
				if (spConfig == null) {
					messageGenerator.addErrorMessage("Es ist keine Host Konfiguration vorhanden", 
									"Betroffener Host: " + hostname);
					return;
				}
				
				sessionManager.setSpId(spConfig.getId());
				sessionManager.setIdpId(idp.getId());
				if (storeIdpSelection != null && storeIdpSelection) {
					cookieHelper.setCookie("preselect_idp", idp.getId().toString(), 356 * 24 * 3600);
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
			else if (selectedIdp instanceof OidcRpConfigurationEntity) {
				OidcRpConfigurationEntity rp = (OidcRpConfigurationEntity) selectedIdp;
				sessionManager.setOidcRelyingPartyId(rp.getId());

				if (storeIdpSelection != null && storeIdpSelection) {
					cookieHelper.setCookie("preselect_idp", rp.getId().toString(), 356 * 24 * 3600);
				}
				else {
					cookieHelper.setCookie("preselect_idp", "", 0);
				}

				try {
					externalContext.redirect("/rpoidc/login");
				} catch (IOException e) {
					messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", 
									e.toString());
				}			
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
				getIdpList().addAll(federationBean.getAllIdpList());
			}
			else if (sessionManager.getAuthnRequestIdpConfigId() != null && 
					sessionManager.getAuthnRequestSpMetadataId() != null) {
				/*
				 * reg-app login called via SAML service provider
				 */
				idpConfig = idpConfigService.findById(sessionManager.getAuthnRequestIdpConfigId());
				spMetadata = spMetadataService.findById(sessionManager.getAuthnRequestSpMetadataId());
				List<ServiceSamlSpEntity> serviceSamlList = idpConfigService.findBySamlSpAndIdp(idpConfig, spMetadata);
				idpList = new ArrayList<Object>();
				
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
				getIdpList().addAll(federationBean.getAllIdpList());
			}
		}
		else {
			if (selectedFederation instanceof FederationEntity) {
				getIdpList().addAll(federationBean.getIdpList((FederationEntity) selectedFederation));
			}
		}
	}
	
	public void chooseOther() {
		setPreSelectedIdp(false);
	}
	
	public List<Object> getFederationList() {
		if (federationList == null) {
			federationList = new ArrayList<Object>();
		}
		return federationList;
	}

	public Object getSelectedFederation() {
		return selectedFederation;
	}

	public void setSelectedFederation(Object selectedFederation) {
		this.selectedFederation = selectedFederation;
		updateIdpList();
	}

	public Object getSelectedIdp() {
		return selectedIdp;
	}

	public void setSelectedIdp(Object selectedIdp) {
		this.selectedIdp = selectedIdp;
	}

	public List<Object> getIdpList() {
		if (filter == null)
			return idpList;
		
		List<Object> filteredList = new ArrayList<Object>();
		
		for (Object o : idpList) {
			if (o instanceof SamlIdpMetadataEntity) {
				SamlIdpMetadataEntity idp = (SamlIdpMetadataEntity) o;
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
			else if (o instanceof OidcRpConfigurationEntity) {
				OidcRpConfigurationEntity rp = (OidcRpConfigurationEntity) o;
				if (rp.getDisplayName() != null &&
						Pattern.compile(
						Pattern.quote(filter), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
						.matcher(rp.getDisplayName()).find()) {
					filteredList.add(rp);
				}
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

	public Boolean getPreSelectedIdp() {
		return preSelectedIdp;
	}

	public void setPreSelectedIdp(Boolean preSelectedIdp) {
		this.preSelectedIdp = preSelectedIdp;
	}

}
