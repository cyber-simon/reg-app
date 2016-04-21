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
import javax.servlet.http.HttpServletRequest;

import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPPostDecoder;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPSOAP11Decoder;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;

import edu.kit.scc.webreg.exc.SamlAuthenticationException;

@ApplicationScoped
public class Saml2DecoderService {

	public Response decodePostMessage(HttpServletRequest request)
			throws MessageDecodingException, SecurityException, SamlAuthenticationException {

		HTTPPostDecoder decoder = new HTTPPostDecoder();
		BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = 
				new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
		HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(request);
		messageContext.setInboundMessageTransport(adapter);
		decoder.decode(messageContext);
		SAMLObject obj = messageContext.getInboundSAMLMessage();
		if (obj instanceof Response) 
			return (Response) obj;
		else
			throw new SamlAuthenticationException("Not a valid SAML2 Post Response");			
	}

	public AttributeQuery decodeAttributeQuery(HttpServletRequest request)
			throws MessageDecodingException, SecurityException, SamlAuthenticationException {

		HTTPSOAP11Decoder decoder = new HTTPSOAP11Decoder();
		BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject> messageContext = 
				new BasicSAMLMessageContext<SAMLObject, SAMLObject, SAMLObject>();
		HttpServletRequestAdapter adapter = new HttpServletRequestAdapter(request);
		messageContext.setInboundMessageTransport(adapter);
		decoder.decode(messageContext);
		SAMLObject obj = messageContext.getInboundSAMLMessage();
		if (obj instanceof AttributeQuery) 
			return (AttributeQuery) obj;
		else
			throw new SamlAuthenticationException("Not a valid SAML2 Attribute Query");			
	}

}
