package edu.kit.scc.webreg.service.oidc.client;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

public interface OidcClientRedirectService extends Serializable {

	void redirectClient(Long oidcRelyingPartyId, HttpServletRequest request, HttpServletResponse response) throws OidcAuthenticationException;

}
