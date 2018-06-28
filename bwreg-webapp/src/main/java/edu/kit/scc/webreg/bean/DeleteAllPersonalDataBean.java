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

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserDeleteService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class DeleteAllPersonalDataBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private SamlUserEntity user;
	
	private List<RegistryEntity> registryList;

	@Inject
	private UserService userService;
	
	@Inject
	private UserDeleteService userDeleteService;
	
	@Inject
	private RegistryService registryService;
	
    @Inject 
    private SessionManager sessionManager;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
	    	UserEntity tempUser = userService.findByIdWithStore(sessionManager.getUserId());
	    	if (tempUser instanceof SamlUserEntity) {
	    		user = (SamlUserEntity) tempUser;
	    	}
	    	else {
	    		throw new IllegalArgumentException("This page is only for SAML Users");
	    	}
	    	registryList = registryService.findByUser(user);
		}
	}
	
	public String cancel() {
		return ViewIds.INDEX_USER;
	}

	public String commit() {
		userDeleteService.deleteUserData(user, "user-" + user.getId());
		return "/logout/local?redirect=delete&faces-redirect=true";
	}
	
	public UserEntity getUser() {
		return user;
	}

	public List<RegistryEntity> getRegistryList() {
		return registryList;
	}

}
