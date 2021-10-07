package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.hook.UserUpdateHook;
import edu.kit.scc.webreg.hook.UserUpdateHookException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.reg.ScriptingWorkflow;
import edu.kit.scc.webreg.session.HttpRequestContext;

public abstract class AbstractUserUpdater<T extends UserEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private ScriptingEnv scriptingEnv;
	
	@Inject
	private HttpRequestContext httpRequest;
	
	public abstract T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, StringBuffer debugLog)
			throws UserUpdateException;

	public abstract T updateUser(T user, Map<String, List<Object>> attributeMap, String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException;

	protected boolean preUpdateUser(UserEntity user, Map<String, List<Object>> attributeMap, Map<String,String> homeOrgGenericStore, 
				String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException {

		boolean returnValue = false;
		
		UserUpdateHook updateHook = resolveUpdateHook(homeOrgGenericStore);
		
		if (updateHook != null) {
			try {
				returnValue |= updateHook.preUpdateUser(user, homeOrgGenericStore, attributeMap, executor, service, null);
			} catch (UserUpdateHookException e) {
				logger.warn("An exception happened while calling UserUpdateHook!", e);
			}
		}
				
		return returnValue;
	}

	protected boolean postUpdateUser(UserEntity user, Map<String, List<Object>> attributeMap, Map<String,String> homeOrgGenericStore, 
				String executor, ServiceEntity service, StringBuffer debugLog)
			throws UserUpdateException {

		boolean returnValue = false;

		if (httpRequest != null && httpRequest.getHttpServletRequest() != null) {
		    user.setLastLoginHost(httpRequest.getHttpServletRequest().getLocalName());
		}
		
		UserUpdateHook updateHook = resolveUpdateHook(homeOrgGenericStore);

		if (updateHook != null) {
			try {
				returnValue |= updateHook.postUpdateUser(user, homeOrgGenericStore, attributeMap, executor, service, null);
			} catch (UserUpdateHookException e) {
				logger.warn("An exception happened while calling UserUpdateHook!", e);
			}
		}
		return returnValue;
	}

	private UserUpdateHook resolveUpdateHook(Map<String,String> homeOrgGenericStore) {
		UserUpdateHook updateHook = null;
		if (homeOrgGenericStore.containsKey("user_update_hook")) {
			String hookClass = homeOrgGenericStore.get("user_update_hook");
			try {
				updateHook = (UserUpdateHook) Class.forName(hookClass).getDeclaredConstructor().newInstance();
				if (updateHook instanceof ScriptingWorkflow)
					((ScriptingWorkflow) updateHook).setScriptingEnv(scriptingEnv);

			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException
					| ClassNotFoundException e) {
				logger.warn("Cannot instantiate updateHook class. This is probably a misconfiguration.");
			}
		}
		
		return updateHook;
	}
}
