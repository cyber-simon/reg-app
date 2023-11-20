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

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@ViewScoped
public class LocalLogoutBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private IdentityEntity identity;
	
	@Inject
	private Logger logger;
	
	@Inject
	private IdentityService identityService;
	
    @Inject 
    private SessionManager sessionManager;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			identity = identityService.fetch(sessionManager.getIdentityId());
		}
	}
	
	public void startLocalLogout() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/logout/local?redirect=local_logout");
		} catch (IOException e) {
			logger.warn("Redirect failed", e);
		}
	}
	
	public IdentityEntity getIdentity() {
		return identity;
	}

}
