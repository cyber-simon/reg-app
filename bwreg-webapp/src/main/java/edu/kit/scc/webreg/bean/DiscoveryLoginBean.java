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

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.FederationService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class DiscoveryLoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FederationService service;
 
	@Inject
	private SamlIdpMetadataService idpService;

	@Inject
	private SamlSpConfigurationService spService;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private List<FederationEntity> federationList;
	private FederationEntity selectedFederation;
	
	private List<SamlIdpMetadataEntity> idpList;
	private SamlIdpMetadataEntity selectedIdp;

	private String filter;
	
	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (sessionManager.getOriginalIdpEntityId() != null) {
			SamlIdpMetadataEntity idp = idpService.findByEntityId(sessionManager.getOriginalIdpEntityId());
			if (idp != null) {
				selectedIdp = idp;
				login();
			}
		}
		
		if (! initialized) {
			federationList = service.findAll();
			if (federationList == null || federationList.size() == 0) {
				messageGenerator.addErrorMessage("Das SAML Subsystem ist noch nicht konfiguriert");
				return;
			}
			//selectedFederation = federationList.get(0);
			updateIdpList();		
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
	
	public void updateIdpList() {
		if (selectedFederation == null) {
			idpList = idpService.findAllByStatusOrderedByOrgname(SamlMetadataEntityStatus.ACTIVE);
		}
		else {
			idpList = idpService.findAllByFederationOrderByOrgname(selectedFederation);
		}
	}
	
	public List<FederationEntity> getFederationList() {
		return federationList;
	}

	public void setFederationList(List<FederationEntity> federationList) {
		this.federationList = federationList;
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

}
