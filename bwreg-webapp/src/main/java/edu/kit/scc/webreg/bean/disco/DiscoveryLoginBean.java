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
package edu.kit.scc.webreg.bean.disco;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.SamlSpMetadataService;
import edu.kit.scc.webreg.service.disco.DiscoveryCacheService;
import edu.kit.scc.webreg.service.disco.UserProvisionerCachedEntry;
import edu.kit.scc.webreg.service.identity.UserProvisionerService;
import edu.kit.scc.webreg.service.oidc.OidcClientConfigurationService;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;
import edu.kit.scc.webreg.service.oidc.OidcRpConfigurationService;
import edu.kit.scc.webreg.service.oidc.ServiceOidcClientService;
import edu.kit.scc.webreg.service.oidc.client.OidcDiscoverySingletonBean;
import edu.kit.scc.webreg.service.saml.FederationSingletonBean;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.CookieHelper;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;

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
	private OidcDiscoverySingletonBean oidcDiscoverySingletonBean;

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
	private ServiceOidcClientService serviceOidcClientService;

	@Inject
	private CookieHelper cookieHelper;

	@Inject
	private DiscoveryCacheService discoveryCache;

	@Inject
	private UserProvisionerService userProvisionerService;

	//private Object selectedIdp;
	private UserProvisionerCachedEntry selected;

	private Boolean storeIdpSelection;

	private String filter;

	/*
	 * Login came from SAML SP. spMetadata holds metadata of requester idpConfig is
	 * the IDP on reg-app side, which was requested
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
				selected = discoveryCache.getEntry(idp.getId());
				login();
				return;
			}
		}

		if (!initialized) {

			discoveryCache.refreshCache();

			if (appConfig.getConfigValue("preselect_store_idp_select") != null
					&& appConfig.getConfigValue("preselect_store_idp_select").equalsIgnoreCase("true")) {
				storeIdpSelection = true;
			} else {
				storeIdpSelection = false;
			}

			if (federationBean.getFederationList() == null || federationBean.getFederationList().size() == 0) {
				messageGenerator.addErrorMessage("Das SAML Subsystem ist noch nicht konfiguriert");
				return;
			}

			updateIdpList();

			Cookie idpCookie = cookieHelper.getCookie("preselect_idp");
			if (idpCookie != null) {
				String cookieValue = idpCookie.getValue();
				cookieValue = cookieValue.replaceAll("[^0-9]", "");

				Long idpId = Long.parseLong(cookieValue);
				SamlIdpMetadataEntity idp = idpService.fetch(idpId);
				if (idp != null) {
					selected = discoveryCache.getEntry(idp.getId());
					storeIdpSelection = true;
				} else {
					OidcRpConfigurationEntity op = oidcRpService.fetch(idpId);
					if (op != null) {
						selected = discoveryCache.getEntry(op.getId());
						storeIdpSelection = true;
					}
				}
			}
			initialized = true;
		}
	}

	public List<UserProvisionerCachedEntry> getExtraList() {
		return discoveryCache.getExtraEntryList();
	}
	
	public List<UserProvisionerCachedEntry> search(String part) {
		return discoveryCache.getUserCountEntryList().stream()
                .filter(o -> o.getName().toLowerCase().contains(part.toLowerCase()))
                .limit(25)
                .collect(Collectors.toList());
	}
	
	public void login() {
		if (selected != null) {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			String hostname = externalContext.getRequestServerName();

			UserProvisionerEntity userProvisioner = userProvisionerService.findByIdWithAttrs(selected.getId());
			
			if (userProvisioner instanceof SamlIdpMetadataEntity) {
				SamlIdpMetadataEntity idp = (SamlIdpMetadataEntity) userProvisioner;
				SamlSpConfigurationEntity spConfig = null;
				List<SamlSpConfigurationEntity> spConfigList = spService.findByHostname(hostname);

				if (spConfigList.size() == 1) {
					spConfig = spConfigList.get(0);
				} else {
					for (SamlSpConfigurationEntity s : spConfigList) {
						if (s.getDefaultSp() != null && s.getDefaultSp().equals(Boolean.TRUE)) {
							spConfig = s;
							break;
						}
					}
				}

				if (spConfig == null) {
					messageGenerator.addErrorMessage("Es ist keine Host Konfiguration vorhanden",
							"Betroffener Host: " + hostname);
					return;
				}

				sessionManager.setSpId(spConfig.getId());
				sessionManager.setIdpId(idp.getId());
				if (storeIdpSelection != null && storeIdpSelection) {
					cookieHelper.setCookie("preselect_idp", idp.getId().toString(), 356 * 24 * 3600);
				} else {
					cookieHelper.setCookie("preselect_idp", "", 0);
				}
				try {
					externalContext.redirect("/Shibboleth.sso/Login");
				} catch (IOException e) {
					messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", e.toString());
				}
			} else if (userProvisioner instanceof OidcRpConfigurationEntity) {
				OidcRpConfigurationEntity rp = (OidcRpConfigurationEntity) userProvisioner;
				sessionManager.setOidcRelyingPartyId(rp.getId());

				if (storeIdpSelection != null && storeIdpSelection) {
					cookieHelper.setCookie("preselect_idp", rp.getId().toString(), 356 * 24 * 3600);
				} else {
					cookieHelper.setCookie("preselect_idp", "", 0);
				}

				try {
					externalContext.redirect("/rpoidc/login");
				} catch (IOException e) {
					messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", e.toString());
				}
			}
		} else {
			messageGenerator.addWarningMessage("Keine Auswahl getroffen", "Bitte wählen Sie Ihre Heimatorganisation");
		}
	}

	public List<Object> updateIdpList() {
		List<Object> idpList = null;
		
		if (sessionManager.getOidcAuthnOpConfigId() != null && sessionManager.getOidcAuthnClientConfigId() != null) {
			/*
			 * reg-app login called via OIDC relying party
			 */
			opConfig = oidcOpConfigService.fetch(sessionManager.getOidcAuthnOpConfigId());
			clientConfig = oidcClientConfigService.fetch(sessionManager.getOidcAuthnClientConfigId());
			List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientService
					.findByClientConfig(clientConfig);
			idpList = new ArrayList<Object>();

			for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
				if (serviceOidcClient.getScript() != null) {
					idpList.addAll(federationBean.getFilteredIdpList(serviceOidcClient.getScript()));

					if (appConfig.getConfigValueOrDefault("show_oidc_login", "false").equalsIgnoreCase("true")) {
						idpList.addAll(oidcDiscoverySingletonBean.getFilteredOpList(serviceOidcClient.getScript()));
					}
				}
			}
		} else if (sessionManager.getAuthnRequestIdpConfigId() != null
				&& sessionManager.getAuthnRequestSpMetadataId() != null) {
			/*
			 * reg-app login called via SAML service provider
			 */
			idpConfig = idpConfigService.fetch(sessionManager.getAuthnRequestIdpConfigId());
			spMetadata = spMetadataService.fetch(sessionManager.getAuthnRequestSpMetadataId());
			List<ServiceSamlSpEntity> serviceSamlList = idpConfigService.findBySamlSpAndIdp(idpConfig, spMetadata);
			idpList = new ArrayList<Object>();

			for (ServiceSamlSpEntity serviceSaml : serviceSamlList) {
				if (serviceSaml.getScript() != null) {
					idpList.addAll(federationBean.getFilteredIdpList(serviceSaml.getScript()));

					if (appConfig.getConfigValueOrDefault("show_oidc_login", "false").equalsIgnoreCase("true")) {
						idpList.addAll(oidcDiscoverySingletonBean.getFilteredOpList(serviceSaml.getScript()));
					}
				}
			}
		} else {
			/*
			 * reg-app login directly called
			 */
			idpList = new ArrayList<Object>();
			idpList.addAll(federationBean.getAllIdpList());

			if (appConfig.getConfigValueOrDefault("show_oidc_login", "false").equalsIgnoreCase("true")) {
				idpList.addAll(oidcDiscoverySingletonBean.getAllOpList());
			}
		}
		sortIdpList(idpList);
		
		return idpList;
	}

	public void addFed(FederationEntity federation) {
		
	}
	
	public void sortIdpList(List<Object> idpList) {
		idpList = idpList.stream().sorted((item1, item2) -> {
			String name1 = ((UserProvisionerEntity) item1).getOrgName();
			String name2 = ((UserProvisionerEntity) item2).getOrgName();
			if (name1 != null)
				return name1.compareTo(name2);
			else
				return 0;
		}).collect(Collectors.toList());
	}

	public List<UserProvisionerCachedEntry> getInitialList() {
		return discoveryCache.getInitialEntryList();
	}
	
	public List<Object> getIdpList() {
		List<Object> idpList = updateIdpList();
		
		if (filter == null)
			return idpList;

		List<Object> filteredList = new ArrayList<Object>();

		for (Object o : idpList) {
			if (o instanceof SamlIdpMetadataEntity) {
				SamlIdpMetadataEntity idp = (SamlIdpMetadataEntity) o;
				if (idp.getOrgName() != null
						&& Pattern.compile(Pattern.quote(filter), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
								.matcher(idp.getOrgName()).find()) {
					filteredList.add(idp);
				} else if (idp.getDisplayName() != null
						&& Pattern.compile(Pattern.quote(filter), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
								.matcher(idp.getDisplayName()).find()) {
					filteredList.add(idp);
				}
			} else if (o instanceof OidcRpConfigurationEntity) {
				OidcRpConfigurationEntity rp = (OidcRpConfigurationEntity) o;
				if (rp.getDisplayName() != null
						&& Pattern.compile(Pattern.quote(filter), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)
								.matcher(rp.getDisplayName()).find()) {
					filteredList.add(rp);
				}
			}
		}

		if (filteredList.size() == 1) {
			//selectedIdp = filteredList.get(0);
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

	public UserProvisionerCachedEntry getSelected() {
		return selected;
	}

	public void setSelected(UserProvisionerCachedEntry selected) {
		this.selected = selected;
	}
}
