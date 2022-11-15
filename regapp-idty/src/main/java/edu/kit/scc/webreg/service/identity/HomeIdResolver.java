package edu.kit.scc.webreg.service.identity;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.hook.UserUpdateHookException;
import edu.kit.scc.webreg.service.impl.AttributeMapHelper;

@ApplicationScoped
public class HomeIdResolver {

	@Inject
	private Logger logger;
	
	@Inject
	private AttributeMapHelper attrHelper;
	
	@Inject
	private IdentityScriptingEnv scriptingEnv;
	
	public String resolveHomeId(UserEntity user, Map<String, List<Object>> attributeMap) {
		String homeId = null;
		
		if (user instanceof SamlUserEntity) {
			SamlUserEntity samlUser = (SamlUserEntity) user;
			if (samlUser.getIdp().getGenericStore().containsKey("prefix")) {
				homeId = samlUser.getIdp().getGenericStore().get("prefix");
			}
			else {
				homeId = attrHelper.getSingleStringFirst(attributeMap, "http://bwidm.de/bwidmOrgId");
			}
		}
		else if (user instanceof OidcUserEntity) {
			OidcUserEntity oidcUser = (OidcUserEntity) user;
			if (oidcUser.getIssuer().getGenericStore().containsKey("prefix")) {
				homeId = oidcUser.getIssuer().getGenericStore().get("prefix");
			}
			else {
				homeId = oidcUser.getIssuer().getName();
			}
		}

		return homeId;
	}

	public String resolvePrimaryGroup(String homeId, UserEntity user, Map<String, List<Object>> attributeMap) {
		String groupName = null;

		if (user instanceof SamlUserEntity) {
			SamlUserEntity samlUser = (SamlUserEntity) user;
			if (samlUser.getIdp().getGenericStore().containsKey("group_resolve_script")) {
				try {
					Invocable invocable = resolveScript(samlUser.getIdp().getGenericStore().get("group_resolve_script"));
					Object o = invocable.invokeFunction("resolvePrimaryGroup", scriptingEnv, user, attributeMap, logger);
					if (o != null && o instanceof String) {
						groupName = (String) o;
					}
				} catch (NoSuchMethodException e) {
					logger.info("No preUpdateUserFromAttribute Method. Skipping execution.");
				} catch (ScriptException | UserUpdateHookException e) {
					logger.info("Script error on preUpdateUserFromAttribute", e);
				}	
			}
		}
		else if (user instanceof OidcUserEntity) {
			OidcUserEntity oidcUser = (OidcUserEntity) user;
			if (oidcUser.getIssuer().getGenericStore().containsKey("group_resolve_script")) {
				try {
					Invocable invocable = resolveScript(oidcUser.getIssuer().getGenericStore().get("group_resolve_script"));
					Object o = invocable.invokeFunction("resolvePrimaryGroup", scriptingEnv, user, attributeMap, logger);
					if (o != null && o instanceof String) {
						groupName = (String) o;
					}
				} catch (NoSuchMethodException e) {
					logger.info("No preUpdateUserFromAttribute Method. Skipping execution.");
				} catch (ScriptException | UserUpdateHookException e) {
					logger.info("Script error on preUpdateUserFromAttribute", e);
				}	
			}
		}		
		return groupName;
	}
	
	protected Invocable resolveScript(String scriptName) throws UserUpdateHookException, ScriptException {
		ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
		
		if (scriptEntity == null)
			throw new UserUpdateHookException("resolve primary group not configured properly. script is missing.");
		
		if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
			ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

			if (engine == null)
				throw new UserUpdateHookException("resolve primary group not configured properly. engine not found: " + scriptEntity.getScriptEngine());
			
			engine.eval(scriptEntity.getScript());
		
			Invocable invocable = (Invocable) engine;

			return invocable;
		}
		else {
			throw new UserUpdateHookException("unkown script type: " + scriptEntity.getScriptType());
		}
	}
	
}
