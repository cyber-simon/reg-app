/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.sec;

import java.io.IOException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.SamlIdpConfigurationService;
import edu.kit.scc.webreg.service.SamlSpMetadataService;
import edu.kit.scc.webreg.service.saml.Saml2DecoderService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SamlIdpService;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

@ApplicationScoped
public class Saml2IdpRedirectHandler {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject
	private SamlIdpService samlIdpService;

	@Inject
	private SamlIdpConfigurationService idpConfigService;
	
	@Inject
	private SamlHelper samlHelper;
	
	@Inject
	private Saml2DecoderService saml2DecoderService;
	
	@Inject
	private SamlSpMetadataService spService;

	@Inject
	private ApplicationConfig appConfig;
	
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<SamlIdpConfigurationEntity> idpConfigList = idpConfigService.findByHostname(request.getServerName());
		SamlIdpConfigurationEntity idpConfig = null;
		for (SamlIdpConfigurationEntity tempIdpConfig : idpConfigList) {
			if (request.getRequestURI().equals(tempIdpConfig.getRedirect())) {
				idpConfig = tempIdpConfig;
				break;
			}
		}
		
		if (idpConfig == null) {
			throw new ServletException("Unknown redirect uri");
		}
		
		AuthnRequest authnRequest;
		
		try {
			authnRequest = saml2DecoderService.decodeRedirectMessage(request);
			logger.debug(samlHelper.prettyPrint(authnRequest));
		} catch (MessageDecodingException | SecurityException | SamlAuthenticationException
				| ComponentInitializationException e) {
			logger.warn("An exception occured", e);
			throw new ServletException(e);
		}
		
		if (authnRequest == null || authnRequest.getIssuer() == null 
				|| authnRequest.getIssuer().getValue() == null) {
			throw new ServletException("SAML Authentication Request ist not complete, issuer data is missing");
		}
		
		SamlSpMetadataEntity spMetadata = spService.findByEntityId(authnRequest.getIssuer().getValue());
		
		if (spMetadata == null) {
			throw new ServletException("Issuer is not known here");
		}

		String relayState = request.getParameter("RelayState");

		logger.debug("Corresponding SP found in Metadata: {}", spMetadata.getEntityId());
		
		if (session == null || session.getIdentityId() == null) {
			logger.debug("Client session from {} not established. In order to serve client must login. Sending to login page.",
					request.getRemoteAddr());
			long id = samlIdpService.registerAuthnRequest(authnRequest);
			session.setAuthnRequestId(id);
			session.setAuthnRequestRelayState(relayState);
			session.setAuthnRequestIdpConfigId(idpConfig.getId());
			session.setAuthnRequestSpMetadataId(spMetadata.getId());
			session.setOriginalRequestPath(idpConfig.getRedirect() + "/response");
			response.sendRedirect("/welcome/index.xhtml");
			return;
		}
		
		long id = samlIdpService.registerAuthnRequest(authnRequest);
		session.setAuthnRequestId(id);
		session.setAuthnRequestRelayState(relayState);
		session.setAuthnRequestIdpConfigId(idpConfig.getId());
		session.setAuthnRequestSpMetadataId(spMetadata.getId());
		response.sendRedirect(request.getRequestURI() + "/response");
	}
}
