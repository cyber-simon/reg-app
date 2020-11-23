package edu.kit.scc.webreg.service.oidc.client;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

public interface OidcClientRedirectService extends Serializable {

	void redirectClient(Long oidcRelyingPartyId, HttpServletRequest request, HttpServletResponse response) throws OidcAuthenticationException;

}
