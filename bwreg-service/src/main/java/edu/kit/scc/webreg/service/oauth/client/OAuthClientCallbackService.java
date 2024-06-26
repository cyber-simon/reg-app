package edu.kit.scc.webreg.service.oauth.client;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

public interface OAuthClientCallbackService extends Serializable {

	void callback(String uri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws OidcAuthenticationException;

}
