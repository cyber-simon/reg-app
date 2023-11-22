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

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.hook.GroupServiceHook;
import edu.kit.scc.webreg.hook.HookManager;
import edu.kit.scc.webreg.hook.UserServiceHook;
import edu.kit.scc.webreg.util.ViewIds;

@Named
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
