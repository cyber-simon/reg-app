package edu.kit.scc.webreg.service.oidc.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.service.oidc.OidcRpConfigurationService;

@Singleton
public class OidcDiscoverySingletonBean {

	@Inject
	private Logger logger;

	@Inject
	private OidcRpConfigurationService oidcRpService;
	
	public List<OidcRpConfigurationEntity> getFilteredOpList(ScriptEntity scriptEntity) {
		
		ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());
		
		List<OidcRpConfigurationEntity> tempList = oidcRpService.findAll();
		Collections.sort(tempList, idpOrgComparator);

		if (engine == null) {
			logger.warn("No engine set for script {}. Returning all IDPs", scriptEntity.getName());
			return tempList;
		}
		
		try {
			List<OidcRpConfigurationEntity> targetList = new ArrayList<OidcRpConfigurationEntity>();

			engine.eval(scriptEntity.getScript());

			Invocable invocable = (Invocable) engine;
			
			invocable.invokeFunction("filterOps", tempList, targetList, logger);

			Collections.sort(targetList, idpOrgComparator);

			return targetList;
		} catch (ScriptException e) {
			logger.warn("Script execution failed.", e);
			return tempList;
		} catch (NoSuchMethodException e) {
			logger.info("No filterOs method in script. returning all Idps");
			return tempList;
		}
	}
	
	private Comparator<OidcRpConfigurationEntity> idpOrgComparator = new Comparator<OidcRpConfigurationEntity>() {

		@Override
		public int compare(OidcRpConfigurationEntity op1, OidcRpConfigurationEntity op2) {
			if (op1 != null && op1.getDisplayName() != null &&
					op2 != null && op2.getDisplayName() != null)
				return op1.getDisplayName().compareTo(op2.getDisplayName());
			else
				return 0;
		}
		
	};	
}
