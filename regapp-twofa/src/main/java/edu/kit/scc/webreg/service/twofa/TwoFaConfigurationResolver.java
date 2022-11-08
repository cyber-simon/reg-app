package edu.kit.scc.webreg.service.twofa;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.script.ScriptingEnv;

@Named("twoFaConfigurationResolver")
@ApplicationScoped
public class TwoFaConfigurationResolver {

	@Inject
	private Logger logger;
	
	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private ScriptingEnv scriptingEnv;

	public Map<String, String> resolveConfig(IdentityEntity identity) throws TwoFaConfigurationResolverException {
		try {
			String scriptName = appConfig.getConfigValue("linotp_resolve_config");

			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);

			if (scriptEntity == null)
				throw new TwoFaConfigurationResolverException("2fa not configured properly. script is missing.");

			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new TwoFaConfigurationResolverException(
							"2fa not configured properly. engine not found: " + scriptEntity.getScriptEngine());

				engine.eval(scriptEntity.getScript());

				Invocable invocable = (Invocable) engine;

				Map<String, String> configMap = new HashMap<String, String>();
				
				invocable.invokeFunction("resolveConfig", scriptingEnv, configMap, identity, logger);
				
				return configMap;
			} else {
				throw new TwoFaConfigurationResolverException("unkown script type: " + scriptEntity.getScriptType());
			}
		} catch (ScriptException e) {
			logger.warn("Script threw error: {}", e.getMessage());
			throw new TwoFaConfigurationResolverException(e);
		} catch (NoSuchMethodException e) {
			logger.warn("Script resolve method is missing: {}", e.getMessage());
			throw new TwoFaConfigurationResolverException(e);
		}
	}
}
