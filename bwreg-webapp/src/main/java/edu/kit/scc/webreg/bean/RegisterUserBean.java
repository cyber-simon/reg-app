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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserCreateService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class RegisterUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private UserService service;

	@Inject
	private UserCreateService userCreateService;

	@Inject
	private SamlIdpMetadataService idpService;

	@Inject
	private SamlSpConfigurationService spService;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private FacesMessageGenerator messageGenerator;

	private SamlUserEntity entity;

	private Boolean errorState = false;
	private Boolean eppnError = false;
	private Boolean eppnOverride = false;

	private Map<String, String> printableAttributesMap;
	private Map<String, String> unprintableAttributesMap;
	private List<String> printableAttributesList;

	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			SamlIdpMetadataEntity idpEntity = idpService.fetch(sessionManager.getIdpId());
			SamlSpConfigurationEntity spConfigEntity = spService.fetch(sessionManager.getSpId());

			try {
				entity = userCreateService.preCreateUser(idpEntity, spConfigEntity, sessionManager.getSamlIdentifier(),
						sessionManager.getLocale(), sessionManager.getAttributeMap());

			} catch (UserUpdateException e) {
				errorState = true;
				messageGenerator.addResolvedErrorMessage("missing-mandatory-attributes", e.getMessage(), true);
				return;
			}

			if (service.findByEppn(entity.getEppn()).size() > 0) {
				eppnError = true;
			}

			printableAttributesMap = new HashMap<String, String>();
			unprintableAttributesMap = new HashMap<String, String>();
			printableAttributesList = new ArrayList<String>();

			attrHelper.convertAttributeNames(sessionManager.getAttributeMap().entrySet(), printableAttributesList,
					printableAttributesMap, unprintableAttributesMap);
		}
	}

	public String save() {

		if (errorState) {
			/*
			 * There are unresolved errors. Cannot persist user.
			 */
			return null;
		} else if (eppnError && (!eppnOverride)) {
			/*
			 * EPPN is already in system, but not aknowledged
			 */
			return null;
		}

		try {
			entity = userCreateService.createUser(entity, sessionManager.getAttributeMap(), null, null);
			entity = userCreateService.postCreateUser(entity, sessionManager.getAttributeMap(), "user-" + entity.getId());
		} catch (UserUpdateException e) {
			logger.warn("An error occured whilst creating user", e);
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			return null;
		}

		sessionManager.setIdentityId(entity.getIdentity().getId());

		if (sessionManager.getOriginalRequestPath() != null) {
			String orig = sessionManager.getOriginalRequestPath();
			sessionManager.setOriginalRequestPath(null);

			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				externalContext.redirect(orig);
			} catch (IOException e) {
				messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			}
			return null;
		} else
			return "/index.xhtml?faces-redirect=true";
	}

	public UserEntity getEntity() {
		return entity;
	}

	public void setEntity(SamlUserEntity entity) {
		this.entity = entity;
	}

	public SamlIdpMetadataEntity getIdpEntity() {
		return idpService.fetch(sessionManager.getIdpId());
	}

	public Boolean getErrorState() {
		return errorState;
	}

	public void setErrorState(Boolean errorState) {
		this.errorState = errorState;
	}

	public Map<String, String> getPrintableAttributesMap() {
		return printableAttributesMap;
	}

	public Map<String, String> getUnprintableAttributesMap() {
		return unprintableAttributesMap;
	}

	public List<String> getPrintableAttributesList() {
		return printableAttributesList;
	}

	public Boolean getEppnOverride() {
		return eppnOverride;
	}

	public void setEppnOverride(Boolean eppnOverride) {
		this.eppnOverride = eppnOverride;
	}

	public Boolean getEppnError() {
		return eppnError;
	}

	public void setEppnError(Boolean eppnError) {
		this.eppnError = eppnError;
	}

	public List<UserEntity> getOldUserList() {
		return service.findByEppn(entity.getEppn());
	}

}
