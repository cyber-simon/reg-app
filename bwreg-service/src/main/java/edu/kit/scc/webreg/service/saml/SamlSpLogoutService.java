package edu.kit.scc.webreg.service.saml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface SamlSpLogoutService {

	void redirectLogout(HttpServletRequest request, HttpServletResponse response, Long userId)
					throws Exception;
	
}
