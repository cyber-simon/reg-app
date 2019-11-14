package edu.kit.scc.webreg.service.saml;

import javax.servlet.http.HttpServletResponse;

import org.opensaml.saml.saml2.core.AuthnRequest;

import edu.kit.scc.webreg.exc.SamlAuthenticationException;

public interface SamlIdpService {

	long registerAuthnRequest(AuthnRequest authnRequest);

	void resumeAuthnRequest(Long authnRequestId, Long userId, Long authnRequestIdpConfigId,
			HttpServletResponse resonse) throws SamlAuthenticationException;

}
