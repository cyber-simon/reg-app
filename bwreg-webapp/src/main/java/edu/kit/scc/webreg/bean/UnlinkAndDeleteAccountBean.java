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

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserDeleteService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class UnlinkAndDeleteAccountBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private IdentityEntity identity;
	private UserEntity user;
	
	private List<RegistryEntity> registryList;

	@Inject
	private Logger logger;
	
	@Inject
	private IdentityService identityService;
	
	@Inject
	private UserService userService;
	
	@Inject
	private UserDeleteService userDeleteService;
	
	@Inject
	private RegistryService registryService;
	
    @Inject 
    private SessionManager sessionManager;
    
	public void preRenderView(ComponentSystemEvent ev) {
	}
	
	public String cancel() {
		return ViewIds.USER_PROPERTIES + "?faces-redirect=true";
	}

	public String commit() {
		userDeleteService.unlinkAndDeleteAccount(getUser(), getIdentity(), "identity-" + getIdentity().getId());
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("/logout/local?redirect=unlink_and_delete_account");
		} catch (IOException e) {
			logger.warn("Redirect failed", e);
		}
		return "";
	}

	public UserEntity getUser() { 
		if (user == null) {
			user = userService.fetch(id);

			if (user == null) {
				throw new IllegalArgumentException("not authorized");
			}

			if (! user.getIdentity().equals(getIdentity())) {
				throw new IllegalArgumentException("not authorized");
			}
		}
		return user;
	}
	
	public List<RegistryEntity> getRegistryList() {
		if (registryList == null)
			registryList = registryService.findByUserAndStatus(getUser(), RegistryStatus.ACTIVE, RegistryStatus.LOST_ACCESS);
		return registryList;
	}

	public IdentityEntity getIdentity() {
		if (identity == null) 
			identity = identityService.fetch(sessionManager.getIdentityId());
		return identity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
