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

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@ViewScoped
public class LogoutBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private IdentityEntity identity;
	
	@Inject
	private Logger logger;
	
	@Inject
	private IdentityService identityService;
	
	@Inject
	private UserService userService;
	
    @Inject 
    private SessionManager sessionManager;
    
    private List<UserEntity> userList;
    private List<UserEntity> userLoginList;
    
	public void preRenderView(ComponentSystemEvent ev) {
	}
	
	public void startLocalLogout() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/logout/local");
		} catch (IOException e) {
			logger.warn("Redirect failed", e);
		}
	}

	public void startLogout() {
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/logout/saml?user_id=1009");
		} catch (IOException e) {
			logger.warn("Redirect failed", e);
		}
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.fetch(sessionManager.getIdentityId());
		}
		
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = userService.findByIdentity(getIdentity());
		}
		return userList;
	}
	
	public List<UserEntity> getUserLoginList() {
		if (userLoginList == null) {
			userLoginList = userService.fetchAll(new ArrayList<Long>(sessionManager.getLoggedInUserList()));
		}
		return userLoginList;
	}
}
