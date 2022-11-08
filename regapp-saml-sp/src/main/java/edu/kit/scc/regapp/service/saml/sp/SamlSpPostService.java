package edu.kit.scc.regapp.service.saml.sp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface SamlSpPostService {

	void consumePost(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig,
			StringBuffer debugLog)
					throws Exception;


	
}
