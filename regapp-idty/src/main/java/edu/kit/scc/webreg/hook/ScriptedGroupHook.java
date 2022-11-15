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
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.identity.IdentityScriptingEnv;

public class ScriptedGroupHook implements GroupServiceHook, IdentityScriptingHookWorkflow {

	private Logger logger = LoggerFactory.getLogger(ScriptedGroupHook.class);
	
	private ApplicationConfig appConfig;
	
	private IdentityScriptingEnv scriptingEnv;
	
	@Override
	public void setAppConfig(ApplicationConfig appConfig) {
		this.appConfig = appConfig;
	}

	@Override
	public GroupEntity preUpdateUserPrimaryGroupFromAttribute(
			HomeOrgGroupDao dao, GroupDao groupDao, GroupEntity group,
			UserEntity user, Map<String, List<Object>> attributeMap,
			Auditor auditor, Set<GroupEntity> changedGroups)
			throws UserUpdateException {
		
		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("preUpdateUserPrimaryGroupFromAttribute", scriptingEnv, group, user, attributeMap, changedGroups, logger);
			if (o instanceof GroupEntity) {
				return (GroupEntity) o;
			}
		} catch (NoSuchMethodException e) {
			logger.info("No preUpdateUserPrimaryGroupFromAttribute Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on preUpdateUserPrimaryGroupFromAttribute", e);
		}
		
		return group;
	}

	@Override
	public GroupEntity postUpdateUserPrimaryGroupFromAttribute(
			HomeOrgGroupDao dao, GroupDao groupDao, GroupEntity group,
			UserEntity user, Map<String, List<Object>> attributeMap,
			Auditor auditor, Set<GroupEntity> changedGroups)
			throws UserUpdateException {

		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("postUpdateUserPrimaryGroupFromAttribute", scriptingEnv, group, user, attributeMap, changedGroups, logger);
			if (o instanceof GroupEntity) {
				return (GroupEntity) o;
			}
		} catch (NoSuchMethodException e) {
			logger.info("No postUpdateUserPrimaryGroupFromAttribute Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on postUpdateUserPrimaryGroupFromAttribute", e);
		}
		
		return group;
	}

	@Override
	public void preUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao,
			GroupDao groupDao, UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor,
			Set<GroupEntity> changedGroups) throws UserUpdateException {

		try {
			Invocable invocable = resolveScript();
			invocable.invokeFunction("preUpdateUserSecondaryGroupFromAttribute", scriptingEnv, user, attributeMap, changedGroups, logger);
		} catch (NoSuchMethodException e) {
			logger.info("No preUpdateUserSecondaryGroupFromAttribute Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on preUpdateUserSecondaryGroupFromAttribute", e);
		}
	}

	@Override
	public void postUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao,
			GroupDao groupDao, UserEntity user,
			Map<String, List<Object>> attributeMap, Auditor auditor,
			Set<GroupEntity> changedGroups) throws UserUpdateException {

		try {
			Invocable invocable = resolveScript();
			invocable.invokeFunction("postUpdateUserSecondaryGroupFromAttribute", scriptingEnv, user, attributeMap, changedGroups, logger);
		} catch (NoSuchMethodException e) {
			logger.info("No postUpdateUserSecondaryGroupFromAttribute Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on isPrimaryResponsible", e);
		}
	}

	@Override
	public boolean isPrimaryResponsible(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("isPrimaryResponsible", scriptingEnv, user, attributeMap, logger);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.info("No isPrimaryResponsible Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on isPrimaryResponsible", e);
		}
		return false;	
	}

	@Override
	public boolean isPrimaryCompleteOverride() {
		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("isPrimaryCompleteOverride", scriptingEnv);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.debug("No isPrimaryCompleteOverride Method. Setting false.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on isPrimaryResponsible", e);
		}
		return false;	
	}

	@Override
	public boolean isSecondaryResponsible(UserEntity user,
			Map<String, List<Object>> attributeMap) {
		
		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("isSecondaryResponsible", scriptingEnv, user, attributeMap, logger);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.info("No isSecondaryResponsible Method. Skipping execution.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on isSecondaryResponsible", e);
		}
		return false;	
	}

	@Override
	public boolean isSecondaryCompleteOverride() {
		try {
			Invocable invocable = resolveScript();
			Object o = invocable.invokeFunction("isSecondaryCompleteOverride", scriptingEnv);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.debug("No isSecondaryCompleteOverride Method. Setting false.");
		} catch (ScriptException | UserUpdateHookException e) {
			logger.info("Script error on isSecondaryCompleteOverride", e);
		}
		return false;	
	}

	protected Invocable resolveScript() throws UserUpdateHookException {
		if (appConfig.getConfigOptions().containsKey("scripted_group_hook_script")) {
			String scriptName = appConfig.getConfigValue("scripted_group_hook_script");

			try {
				ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
				
				if (scriptEntity == null)
					throw new UserUpdateHookException("group update hook not configured properly. script is missing.");
				
				if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
					ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());
	
					if (engine == null)
						throw new UserUpdateHookException("group update hook not configured properly. engine not found: " + scriptEntity.getScriptEngine());
					
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
			throw new UserUpdateHookException("scripted_group_hook_script not configured");
		}
	}

	@Override
	public void setScriptingEnv(IdentityScriptingEnv scriptingEnv) {
		this.scriptingEnv = scriptingEnv;
	}
}
