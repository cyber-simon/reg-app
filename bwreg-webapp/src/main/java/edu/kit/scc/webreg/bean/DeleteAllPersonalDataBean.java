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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserDeleteService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class DeleteAllPersonalDataBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private IdentityEntity identity;
	
	private List<RegistryEntity> registryList;

	@Inject
	private Logger logger;
	
	@Inject
	private IdentityService identityService;
	
	@Inject
	private UserDeleteService userDeleteService;
	
	@Inject
	private RegistryService registryService;
	
    @Inject 
    private SessionManager sessionManager;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			identity = identityService.findById(sessionManager.getIdentityId());
	    	registryList = registryService.findByIdentity(identity);
		}
	}
	
	public String cancel() {
		return ViewIds.INDEX_USER;
	}

	public String commit() {
		userDeleteService.deleteUserData(identity, "identity-" + identity.getId());
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/logout/local?redirect=delete");
		} catch (IOException e) {
			logger.warn("Redirect failed", e);
		}
		return "";
	}
	
	public List<RegistryEntity> getRegistryList() {
		return registryList;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

}
