package edu.kit.scc.webreg.service.oidc.client;

import java.io.Serializable;

import javax.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

public interface OidcClientRedirectService extends Serializable {

	void redirectClient(Long oidcRelyingPartyId, HttpServletResponse response) throws OidcAuthenticationException;

}
