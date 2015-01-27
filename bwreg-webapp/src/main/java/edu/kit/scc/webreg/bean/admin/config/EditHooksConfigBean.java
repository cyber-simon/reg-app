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
package edu.kit.scc.webreg.bean.admin.config;

import java.io.Serializable;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.service.GroupServiceHook;
import edu.kit.scc.webreg.service.UserServiceHook;
import edu.kit.scc.webreg.service.impl.HookManager;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class EditHooksConfigBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private HookManager hookManager;
	
	private Boolean initialized = false;
	
	private String userHooks;
	private String groupHooks;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			userHooks = appConfig.getConfigValue("user_hooks");
			groupHooks = appConfig.getConfigValue("group_hooks");
			initialized = true;
		}
	}
	
	public String save() {
		if (userHooks == null || userHooks.equals("")) {
			appConfig.deleteConfigValue("user_hooks");
		}
		else {
			appConfig.storeConfigValue("user_hooks", userHooks);
		}
		
		if (groupHooks == null || groupHooks.equals("")) {
			appConfig.deleteConfigValue("group_hooks");
		}
		else {
			appConfig.storeConfigValue("group_hooks", groupHooks);
		}	
		
		return ViewIds.CONFIG_INDEX + "?faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.CONFIG_INDEX + "?faces-redirect=true";
	}

	public String getUserHooks() {
		return userHooks;
	}

	public void setUserHooks(String userHooks) {
		this.userHooks = userHooks;
	}

	public String getGroupHooks() {
		return groupHooks;
	}

	public void setGroupHooks(String groupHooks) {
		this.groupHooks = groupHooks;
	}

	public Set<UserServiceHook> getInplaceUserHooks() {
		return hookManager.getUserHooks();
	}

	public Set<GroupServiceHook> getInplaceGroupHooks() {
		return hookManager.getGroupHooks();
	}
}
