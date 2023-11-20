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
package edu.kit.scc.webreg.hook;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.service.identity.IdentityScriptingEnv;

@ApplicationScoped
public class HookManager implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private IdentityScriptingEnv scriptingEnv;

	private Set<UserServiceHook> userHooks;
	private Set<GroupServiceHook> groupHooks;

	@PostConstruct
	public void init() {
		groupHooks = new HashSet<GroupServiceHook>();
		userHooks = new HashSet<UserServiceHook>();
	}
	
	public void reloadHooks() {
		logger.info("Reloading User Hooks");
		reloadUserHooks();
		logger.info("Reloading Group Hooks");
		reloadGroupHooks();
	}
	
	public void reloadUserHooks() {
		Set<UserServiceHook> newUserHooks = new HashSet<UserServiceHook>();

		String hooksString = appConfig.getConfigValue("user_hooks");
		if (hooksString != null && hooksString.length() > 0) {
			hooksString = hooksString.trim();
			String[] hooks = hooksString.split(";");
			for (String hook : hooks) {
				hook = hook.trim();
				try {
					logger.debug("installing hook {}", hook);
					UserServiceHook h = (UserServiceHook) Class.forName(hook).getConstructor().newInstance();
					if (h instanceof IdentityScriptingHookWorkflow) {
						((IdentityScriptingHookWorkflow) h).setScriptingEnv(scriptingEnv);
					}
					h.setAppConfig(appConfig);
					newUserHooks.add(h);
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
					logger.warn("Could not spawn hook " + hook, e);
				} 
			}
		}
		
		userHooks = newUserHooks;
	}
	
	public void reloadGroupHooks() {
		Set<GroupServiceHook> newGroupHooks = new HashSet<GroupServiceHook>();
		
		String hooksString = appConfig.getConfigValue("group_hooks");
		if (hooksString != null && hooksString.length() > 0) {
			hooksString = hooksString.trim();
			String[] hooks = hooksString.split(";");
			for (String hook : hooks) {
				hook = hook.trim();
				try {
					logger.debug("installing hook {}", hook);
					GroupServiceHook h = (GroupServiceHook) Class.forName(hook).getConstructor().newInstance();
					if (h instanceof IdentityScriptingHookWorkflow) {
						((IdentityScriptingHookWorkflow) h).setScriptingEnv(scriptingEnv);
					}
					h.setAppConfig(appConfig);
					newGroupHooks.add(h);
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
					logger.warn("Could not spawn hook " + hook, e);
				} 
			}
		}
		
		groupHooks = newGroupHooks;
	}
	
	public Set<GroupServiceHook> getGroupHooks() {
		return groupHooks;
	}

	public Set<UserServiceHook> getUserHooks() {
		return userHooks;
	}
	
}
