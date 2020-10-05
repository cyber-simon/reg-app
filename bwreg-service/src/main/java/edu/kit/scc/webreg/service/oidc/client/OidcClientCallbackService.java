package edu.kit.scc.webreg.service.oidc.client;

import java.io.Serializable;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

public interface OidcClientCallbackService extends Serializable {

	void callback(String uri) throws OidcAuthenticationException;

}
