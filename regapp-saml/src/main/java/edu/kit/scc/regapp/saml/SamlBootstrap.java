package edu.kit.scc.regapp.saml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.slf4j.Logger;

@ApplicationScoped
public class SamlBootstrap {

	@Inject
	Logger logger;

	public void init() {
    	try {
    		logger.info("OpenSAML Bootstrap...");
			InitializationService.initialize();
				        
		} catch (InitializationException e) {
			logger.error("Serious Error happened", e);
		}
	}
	
}
