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
import javax.servlet.http.HttpServletResponse;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncoder;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.profile.action.MessageEncoderFactory;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;

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
	
	public void redirectClient(SamlIdpMetadataEntity idpEntity,
			SamlSpConfigurationEntity spEntity, HttpServletResponse response) 
					throws MessageEncodingException {

		EntityDescriptor entityDesc = samlHelper.unmarshal(
				idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		SingleSignOnService sso = metadataHelper.getSSO(entityDesc, SAMLConstants.SAML2_REDIRECT_BINDING_URI);

		AuthnRequest authnRequest = ssoHelper.buildAuthnRequest(
				spEntity.getEntityId(), spEntity.getAcs(), SAMLConstants.SAML2_POST_BINDING_URI);

		logger.debug("Sending client to idp {} endpoint {}", idpEntity.getEntityId(), sso.getLocation());
		MessageContext<AuthnRequest> messageContext = new MessageContext<AuthnRequest>();
		SAMLBindingContext bindingContext = new SAMLBindingContext();
		bindingContext.setBindingDescriptor(sso);
		
		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		messageContext.setMessage(authnRequest);
		messageContext. setPeerEntityEndpoint(sso);
		//messageContext.setOutboundMessageTransport(new HttpServletResponseAdapter(response, true));
		encoder.encode(messageContext);
		
	}

}
