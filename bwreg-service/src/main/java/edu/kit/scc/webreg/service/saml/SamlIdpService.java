package edu.kit.scc.webreg.service.saml;

import jakarta.servlet.http.HttpServletResponse;

import org.opensaml.saml.saml2.core.AuthnRequest;

import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;

public interface SamlIdpService {

	long registerAuthnRequest(AuthnRequest authnRequest);

	String resumeAuthnRequest(Long authnRequestId, Long userId, Long authnRequestIdpConfigId,
			String relayState, HttpServletResponse resonse) throws SamlAuthenticationException;

}
