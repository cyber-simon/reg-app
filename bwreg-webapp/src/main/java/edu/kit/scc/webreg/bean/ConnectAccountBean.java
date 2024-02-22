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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.UserProvisionerEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.disco.DiscoveryCacheService;
import edu.kit.scc.webreg.service.disco.UserProvisionerCachedEntry;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.identity.UserProvisionerService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ConnectAccountBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserService userService;

	@Inject
	private IdentityService identityService;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private SamlSpConfigurationService spService;

	@Inject
	private DiscoveryCacheService discoveryCache;

	@Inject
	private UserProvisionerService userProvisionerService;

	@Inject
	private FacesMessageGenerator messageGenerator;

	@Inject
	private ApplicationConfig appConfig;

	private IdentityEntity identity;
	private List<UserEntity> userList;

	private UserProvisionerCachedEntry selected;

	private Boolean initialized = false;
	private Boolean largeList = false;

	public void preRenderView(ComponentSystemEvent ev) {
		if (!initialized) {

			discoveryCache.refreshCache();
			Integer largeLimit = Integer
					.parseInt(appConfig.getConfigValueOrDefault("discovery_large_list_threshold", "100"));
			if (getAllList().size() > largeLimit)
				largeList = true;

			initialized = true;
		}
	}

	public void startConnect() {
		if (selected != null) {
			startConnect(selected.getId());
		} else {
			messageGenerator.addWarningMessage("Keine Auswahl getroffen", "Bitte wählen Sie Ihre Heimatorganisation");
		}
	}

	public void startConnect(Long userProvisionerId) {
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
			sessionManager.setSpId(spConfig.getId());
			sessionManager.setIdpId(idp.getId());
			try {
				externalContext.redirect("/Shibboleth.sso/Login");
			} catch (IOException e) {
				messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", e.toString());
			}

		} else if (userProvisioner instanceof OidcRpConfigurationEntity) {
			OidcRpConfigurationEntity rp = (OidcRpConfigurationEntity) userProvisioner;
			sessionManager.setOidcRelyingPartyId(rp.getId());
			try {
				externalContext.redirect("/rpoidc/login");
			} catch (IOException e) {
				messageGenerator.addErrorMessage("Ein Fehler ist aufgetreten", e.toString());
			}
		} else {
			messageGenerator.addWarningMessage("Keine Auswahl getroffen", "Bitte wählen Sie Ihre Heimatorganisation");
		}

	}

	public List<UserProvisionerCachedEntry> getExtraList() {
		return discoveryCache.getExtraEntryList(new ArrayList<>());
	}

	public List<UserProvisionerCachedEntry> getAllList() {
		return discoveryCache.getAllEntryList(new ArrayList<>());
	}

	public List<UserProvisionerCachedEntry> search(String part) {
		final Set<String> idps = new HashSet<>();
		idps.addAll(getUserList().stream().filter(o -> (o instanceof SamlUserEntity))
				.map(o -> ((SamlUserEntity) o).getIdp().getEntityId()).toList());
		idps.addAll(getUserList().stream().filter(o -> (o instanceof OidcUserEntity))
				.map(o -> ((OidcUserEntity) o).getIssuer().getName()).toList());
		return discoveryCache.getUserCountEntryList(new ArrayList<>()).stream()
				.filter(o -> o.getDisplayName().toLowerCase().contains(part.toLowerCase()))
				.filter(o -> (! idps.contains(o.getEntityId()))).limit(25)
				.collect(Collectors.toList());
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.fetch(sessionManager.getIdentityId());
		}
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = userService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(UserEntity_.id)),
					equal(UserEntity_.identity, getIdentity()), UserEntity_.genericStore, UserEntity_.attributeStore,
					SamlUserEntity_.idp);
		}
		return userList;
	}

	public Boolean getLargeList() {
		return largeList;
	}

	public UserProvisionerCachedEntry getSelected() {
		return selected;
	}

	public void setSelected(UserProvisionerCachedEntry selected) {
		this.selected = selected;
	}

}
