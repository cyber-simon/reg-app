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

import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.reg.ScriptingWorkflow;

public class ScriptedUserHook implements UserServiceHook, ScriptingWorkflow {

	private Logger logger = LoggerFactory.getLogger(ScriptedUserHook.class);
	
	private ApplicationConfig appConfig;
	
	private ScriptingEnv scriptingEnv;
	
	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public void preUpdateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {

		try {
			Invocable invocable = resolveScript();
			invocable.invokeFunction("preUpdateUserFromAttribute", scriptingEnv, user, attributeMap, logger);
		} catch (NoSuchMethodException e) {
			logger.info("No preUpdateUserFromAttribute Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on preUpdateUserFromAttribute", e);
		}
	}

	@Override
	public void postUpdateUserFromAttribute(UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor)
			throws UserUpdateException {
		
		try {
			Invocable invocable = resolveScript();
			invocable.invokeFunction("postUpdateUserFromAttribute", scriptingEnv, user, attributeMap, logger);
		} catch (NoSuchMethodException e) {
			logger.info("No postUpdateUserFromAttribute Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on postUpdateUserFromAttribute", e);
		}
	}

	@Override
	public boolean isResponsible(UserEntity user,
			Map<String, List<Object>> attributeMap) {

		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("isResponsible", scriptingEnv, user, attributeMap, logger);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.info("No isResponsible Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on isResponsible", e);
		}
		return false;	
	}

	@Override
	public boolean isCompleteOverride() {
		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("isCompleteOverride", scriptingEnv);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.debug("No isCompleteOverride Method. Setting false.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on isCompleteOverride", e);
		}
		return false;	
	}

	protected Invocable resolveScript() throws UserUpdateHookException {
		if (appConfig.getConfigOptions().containsKey("scripted_user_hook_script")) {
			String scriptName = appConfig.getConfigValue("scripted_user_hook_script");

			try {
				ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
				
				if (scriptEntity == null)
					throw new UserUpdateHookException("user update hook not configured properly. script is missing.");
				
				if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
					ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());
	
					if (engine == null)
						throw new UserUpdateHookException("user update hook not configured properly. engine not found: " + scriptEntity.getScriptEngine());
					
					engine.eval(scriptEntity.getScript());
				
					Invocable invocable = (Invocable) engine;
	
					return invocable;
				}
				else {
					throw new UserUpdateHookException("unkown script type: " + scriptEntity.getScriptType());
				}
			} catch (ScriptException e) {
				throw new UserUpdateHookException(e);
			}		
		}
		else {
			throw new UserUpdateHookException("scripted_user_hook_script not configured");
		}
	}

	@Override
	public void setScriptingEnv(ScriptingEnv env) {
		this.scriptingEnv = env;
	}

}
