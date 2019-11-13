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
import java.io.InputStream;
import java.net.URL;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.Saml2DecoderService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SamlIdpService;
import edu.kit.scc.webreg.service.saml.SsoHelper;
import edu.kit.scc.webreg.session.SessionManager;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

@ApplicationScoped
public class Saml2IdpRedirectResponseHandler {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject
	private SamlIdpService samlIdpService;

	@Inject
	private SamlHelper samlHelper;
	
	@Inject
	private SsoHelper ssoHelper;
	
	@Inject
	private Saml2DecoderService saml2DecoderService;
	
	@Inject
	private ApplicationConfig appConfig;
	
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (session.getAuthnRequestId() == null) {
			logger.warn("No AuthnRequestId set in session. Cannot continue");
			return;
		}
		
		AuthnRequest authnRequest = samlIdpService.resumeAuthnRequest(session.getAuthnRequestId());
		
		logger.debug("Authn request reloaded: {}", samlHelper.prettyPrint(authnRequest));
		HTTPPostEncoder postEncoder = new HTTPPostEncoder();
		postEncoder.setHttpServletResponse(response);
		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();
		Response samlResponse = ssoHelper.buildAuthnResponse(authnRequest, "https://bwidm.scc.kit.edu/saml/idp/metadata");
		messageContext.setMessage(samlResponse);

		SAMLPeerEntityContext entityContext = new SAMLPeerEntityContext();
		//entityContext.setEntityId("http://test");
		SAMLEndpointContext endpointContext = new SAMLEndpointContext();
		
		AssertionConsumerService acs = samlHelper.create(AssertionConsumerService.class, AssertionConsumerService.DEFAULT_ELEMENT_NAME);
		acs.setLocation(authnRequest.getAssertionConsumerServiceURL());
		endpointContext.setEndpoint(acs);
		entityContext.addSubcontext(endpointContext);
		messageContext.addSubcontext(entityContext);

		postEncoder.setMessageContext(messageContext);

		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.setProperty("resource.loader", "class");
		engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init();
		postEncoder.setVelocityEngine(engine);
		
		logger.debug(samlHelper.prettyPrint(samlResponse));
		
		try {
			postEncoder.initialize();
			postEncoder.encode();
		} catch (MessageEncodingException | ComponentInitializationException e) {
			logger.warn("Exception occured", e);
			throw new ServletException(e);
		}
	}
}
