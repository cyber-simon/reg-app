package edu.kit.scc.webreg.service.oauth.client;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

public interface OAuthClientRedirectService extends Serializable {

	void redirectClient(Long oidcRelyingPartyId, HttpServletRequest request, HttpServletResponse response) throws OidcAuthenticationException;

}
