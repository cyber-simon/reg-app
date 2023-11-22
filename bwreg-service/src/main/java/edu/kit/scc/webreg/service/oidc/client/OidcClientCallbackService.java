package edu.kit.scc.webreg.service.oidc.client;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

public interface OidcClientCallbackService extends Serializable {

	void callback(String uri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws OidcAuthenticationException;

}
