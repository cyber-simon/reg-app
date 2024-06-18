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
import java.util.stream.Collectors;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthRpConfigurationEntity;
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
	private ServiceOidcClientService serviceOidcClientService;

	@Inject
	private CookieHelper cookieHelper;

	@Inject
	private DiscoveryCacheService discoveryCache;

	@Inject
	private UserProvisionerService userProvisionerService;

	// private Object selectedIdp;
	private UserProvisionerCachedEntry selected;

	private Boolean storeIdpSelection;

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

	// Filter IDPs and OPs if this list is populated
	private List<ScriptEntity> filterScriptList;

	private Boolean initialized = false;
	private Boolean largeList = false;

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
			filterScriptList = new ArrayList<>();

			if (appConfig.getConfigValueOrDefault("preselect_store_idp_select", "false").equalsIgnoreCase("true")) {
				storeIdpSelection = true;
			} else {
				storeIdpSelection = false;
			}

			if (discoveryCache.getAllEntryList(filterScriptList) == null
					|| discoveryCache.getAllEntryList(filterScriptList).size() == 0) {
				messageGenerator.addErrorMessage("Das SAML Subsystem ist noch nicht konfiguriert");
				return;
			}

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

			if (sessionManager.getOidcAuthnOpConfigId() != null
					&& sessionManager.getOidcAuthnClientConfigId() != null) {
				/*
				 * reg-app login called via OIDC relying party
				 */
				opConfig = oidcOpConfigService.fetch(sessionManager.getOidcAuthnOpConfigId());
				clientConfig = oidcClientConfigService.fetch(sessionManager.getOidcAuthnClientConfigId());
				List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientService
						.findByClientConfig(clientConfig);

				for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
					if (serviceOidcClient.getScript() != null) {
						filterScriptList.add(serviceOidcClient.getScript());
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

				for (ServiceSamlSpEntity serviceSaml : serviceSamlList) {
					if (serviceSaml.getScript() != null) {
						filterScriptList.add(serviceSaml.getScript());
					}
				}
			}

			Integer largeLimit = Integer
					.parseInt(appConfig.getConfigValueOrDefault("discovery_large_list_threshold", "100"));
			if (getAllList().size() > largeLimit)
				largeList = true;

			initialized = true;
		}
	}

	public List<UserProvisionerCachedEntry> getExtraList() {
		return discoveryCache.getExtraEntryList(filterScriptList);
	}

	public List<UserProvisionerCachedEntry> getAllList() {
		return discoveryCache.getAllEntryList(filterScriptList);
	}

	public List<UserProvisionerCachedEntry> search(String part) {
		return discoveryCache.getUserCountEntryList(filterScriptList).stream()
				.filter(o -> o.getDisplayName().toLowerCase().contains(part.toLowerCase())).limit(25)
				.collect(Collectors.toList());
	}

	public void login(Long userProvisionerId) {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		String hostname = externalContext.getRequestServerName();

		UserProvisionerEntity userProvisioner = userProvisionerService.findByIdWithAttrs(userProvisionerId);

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
		} else if (userProvisioner instanceof OAuthRpConfigurationEntity) {
			OAuthRpConfigurationEntity rp = (OAuthRpConfigurationEntity) userProvisioner;
			sessionManager.setOauthRelyingPartyId(rp.getId());

			if (storeIdpSelection != null && storeIdpSelection) {
				cookieHelper.setCookie("preselect_idp", rp.getId().toString(), 356 * 24 * 3600);
			} else {
				cookieHelper.setCookie("preselect_idp", "", 0);
			}

			try {
				externalContext.redirect("/rpoauth/login");
			} catch (IOException e) {
				messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", e.toString());
			}
		}

	}

	public void login() {
		if (selected != null) {
			login(selected.getId());
		} else {
			messageGenerator.addWarningMessage("Keine Auswahl getroffen", "Bitte wählen Sie Ihre Heimatorganisation");
		}
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

	public Boolean getLargeList() {
		return largeList;
	}
}
