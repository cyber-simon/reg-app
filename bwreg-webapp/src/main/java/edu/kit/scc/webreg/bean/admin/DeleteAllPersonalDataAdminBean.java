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
package edu.kit.scc.webreg.bean.admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
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

@Named
@ViewScoped
public class DeleteAllPersonalDataAdminBean implements Serializable {

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

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			identity = identityService.findById(id);
	    	registryList = registryService.findByIdentity(identity);
		}
	}
	
	public String cancel() {
		return "/admin/?faces-redirect=true";
	}

	public String commit() {
		userDeleteService.deleteUserData(identity, "admin-" + sessionManager.getIdentityId());
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/admin/");
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
