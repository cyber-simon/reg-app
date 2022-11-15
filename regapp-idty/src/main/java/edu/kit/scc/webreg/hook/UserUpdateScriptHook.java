package edu.kit.scc.webreg.hook;

import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.identity.IdentityScriptingEnv;

public class UserUpdateScriptHook implements UserUpdateHook, IdentityScriptingHookWorkflow {

	private static final Logger logger = LoggerFactory.getLogger(UserUpdateScriptHook.class);
	
	protected IdentityScriptingEnv scriptingEnv;
	
	@Override
	public boolean preUpdateUser(UserEntity user, Map<String, String> genericStore, Map<String, List<Object>> attributeMap, String executor,
			ServiceEntity service, StringBuffer debugLog) throws UserUpdateHookException {
		logger.debug("UserUpdateScriptHook preUpdateUser called");

		Invocable invocable = resolveScript(genericStore);

		try {
			Object o = invocable.invokeFunction("preUpdateUser", scriptingEnv, user, genericStore, attributeMap, service, logger, debugLog);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.info("No preUpdateUser Method. Skipping execution.");
		} catch (ScriptException e) {
			throw new UserUpdateHookException(e);
		}
		return false;
	}

	@Override
	public boolean postUpdateUser(UserEntity user, Map<String, String> genericStore, Map<String, List<Object>> attributeMap, String executor,
			ServiceEntity service, StringBuffer debugLog) throws UserUpdateHookException {
		logger.debug("UserUpdateScriptHook postUpdateUser called");

		Invocable invocable = resolveScript(genericStore);

		try {
			Object o = invocable.invokeFunction("postUpdateUser", scriptingEnv, user, genericStore, attributeMap, service, logger, debugLog);
			if (o instanceof Boolean) {
				return (Boolean) o;
			}
		} catch (NoSuchMethodException e) {
			logger.info("No preUpdateUser Method. Skipping execution.");
		} catch (ScriptException e) {
			throw new UserUpdateHookException(e);
		}
		return false;
	}

	@Override
	public void setScriptingEnv(IdentityScriptingEnv env) {
		this.scriptingEnv = env;
	}

	protected Invocable resolveScript(Map<String, String> genericStore) throws UserUpdateHookException {
		if (genericStore.containsKey("user_update_hook_script")) {
			String scriptName = genericStore.get("user_update_hook_script");

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
			throw new UserUpdateHookException("user_update_hook_script not configured");
		}
	}
}
