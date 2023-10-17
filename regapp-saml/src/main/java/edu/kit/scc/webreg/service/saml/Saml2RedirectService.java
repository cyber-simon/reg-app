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
package edu.kit.scc.webreg.service.saml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.VelocityEngine;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

@ApplicationScoped
public class Saml2RedirectService {

	@Inject
	private Logger logger;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private MetadataHelper metadataHelper;

	@Inject
	private SsoHelper ssoHelper;

	public void redirectClient(SamlIdpMetadataEntity idpEntity, SamlSpConfigurationEntity spEntity,
			HttpServletRequest request, HttpServletResponse response)
			throws MessageEncodingException, ComponentInitializationException {

		String acs;
		if (!spEntity.getAcs().startsWith("https://")) {
			/*
			 * we are dealing with a relative acs endpoint. We have to build it with the
			 * called hostname;
			 */
			acs = "https://" + request.getServerName() + spEntity.getAcs();
		} else {
			acs = spEntity.getAcs();
		}

		EntityDescriptor entityDesc = samlHelper.unmarshal(idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		SingleSignOnService sso = null;

		if (idpEntity.getGenericStore().containsKey("prefer_binding")
				&& idpEntity.getGenericStore().get("prefer_binding").equalsIgnoreCase("post")) {
			sso = metadataHelper.getSSO(entityDesc, SAMLConstants.SAML2_POST_BINDING_URI);
		} else {
			sso = metadataHelper.getSSO(entityDesc, SAMLConstants.SAML2_REDIRECT_BINDING_URI);
		}

		AuthnRequest authnRequest = ssoHelper.buildAuthnRequest(spEntity.getEntityId(), acs,
				SAMLConstants.SAML2_POST_BINDING_URI, idpEntity.getGenericStore(), sso.getLocation());
		logger.debug("Sending client to idp {} endpoint {} and ACS {}", idpEntity.getEntityId(), sso.getLocation(),
				acs);

		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();
		messageContext.setMessage(authnRequest);
		SAMLPeerEntityContext entityContext = new SAMLPeerEntityContext();
		entityContext.setEntityId(idpEntity.getEntityId());
		SAMLEndpointContext endpointContext = new SAMLEndpointContext();
		endpointContext.setEndpoint(sso);
		entityContext.addSubcontext(endpointContext);
		messageContext.addSubcontext(entityContext);

		if (idpEntity.getGenericStore().containsKey("prefer_binding")
				&& idpEntity.getGenericStore().get("prefer_binding").equalsIgnoreCase("post")) {
			VelocityEngine engine = new VelocityEngine();
			engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
			engine.setProperty("resource.loader", "class");
			engine.setProperty("class.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			engine.init();
			
			HTTPPostEncoder encoder = new HTTPPostEncoder();
			encoder.setVelocityEngine(engine);
			//encoder.setVelocityTemplateId("templates/saml2-post-binding.vm");
			encoder.setHttpServletResponse(response);
			encoder.setMessageContext(messageContext);
			encoder.initialize();
			encoder.prepareContext();
			encoder.encode();
		} else {
			HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
			encoder.setHttpServletResponse(response);
			encoder.setMessageContext(messageContext);
			encoder.initialize();
			encoder.prepareContext();
			encoder.encode();
		}
	}

}
