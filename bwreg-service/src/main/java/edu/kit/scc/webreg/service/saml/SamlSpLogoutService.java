package edu.kit.scc.webreg.service.saml;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

public interface SamlSpLogoutService {

	void redirectLogout(HttpServletRequest request, HttpServletResponse response, Long userId)
					throws Exception;

	void consumeRedirectLogout(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig)
			throws Exception;
	
}
