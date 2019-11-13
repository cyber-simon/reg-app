package edu.kit.scc.webreg.service.saml;

import org.opensaml.saml.saml2.core.AuthnRequest;

public interface SamlIdpService {

	long registerAuthnRequest(AuthnRequest authnRequest);

	AuthnRequest resumeAuthnRequest(Long authnRequestId);

}
