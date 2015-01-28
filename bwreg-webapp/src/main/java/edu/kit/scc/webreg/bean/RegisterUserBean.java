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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserCreateService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import edu.kit.scc.webreg.util.SessionManager;

@ManagedBean
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
	private FacesMessageGenerator messageGenerator;
	
	private UserEntity entity;
	private SamlIdpMetadataEntity idpEntity;
	private SamlSpConfigurationEntity spConfigEntity;
	
	private Boolean errorState = false;
	
    public void preRenderView(ComponentSystemEvent ev) {
    	idpEntity = idpService.findById(sessionManager.getIdpId());
    	spConfigEntity = spService.findById(sessionManager.getSpId());
    	
    	try {
        	entity = userCreateService.preCreateUser(idpEntity, spConfigEntity, sessionManager.getPersistentId(),
        			sessionManager.getLocale(), sessionManager.getAttributeMap());
        	
		} catch (RegisterException e) {
			errorState = true;
			messageGenerator.addResolvedErrorMessage("missing-mandatory-attributes", e.getMessage(), true);
			return;
		}
    	
    	if (service.findByEppn(entity.getEppn()) != null) {
			errorState = true;
			messageGenerator.addResolvedErrorMessage("eppn-blocked", "eppn-blocked-detail", true);
    	}
    	
	}

    public String save() {

		try {
			entity = userCreateService.createUser(entity, sessionManager.getAttributeMap(), null);
		} catch (RegisterException e) {
			logger.warn("An error occured whilst creating user", e);
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			return null;
		}

    	sessionManager.setUserId(entity.getId());
    	
		if (sessionManager.getOriginalRequestPath() != null) {
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				externalContext.redirect(sessionManager.getOriginalRequestPath());
			} catch (IOException e) {
				messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			}
			return null;
		}
		else
			return "/index.xhtml?faces-redirect=true";
    }
    
	public UserEntity getEntity() {
		return entity;
	}

	public void setEntity(UserEntity entity) {
		this.entity = entity;
	}

	public SamlIdpMetadataEntity getIdpEntity() {
		return idpEntity;
	}

	public Boolean getErrorState() {
		return errorState;
	}

	public void setErrorState(Boolean errorState) {
		this.errorState = errorState;
	}

	
}
