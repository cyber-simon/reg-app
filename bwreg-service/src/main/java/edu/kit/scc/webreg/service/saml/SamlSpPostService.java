package edu.kit.scc.webreg.service.saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface SamlSpPostService {

	void consumePost(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig,
			StringBuffer debugLog)
					throws Exception;


	
}
