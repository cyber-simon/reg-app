package edu.kit.scc.webreg.service.saml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface SamlSpPostService {

	void consumePost(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig)
			throws Exception;


	
}
