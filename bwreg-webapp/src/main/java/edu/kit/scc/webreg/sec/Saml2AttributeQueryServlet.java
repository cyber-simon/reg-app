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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.Configuration;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.security.SecurityException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserUpdateService;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.Saml2DecoderService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.util.SessionManager;

@Named
@WebServlet(urlPatterns = {"/Shibboleth.sso/SAML2/AttributeQuery"})
public class Saml2AttributeQueryServlet implements Servlet {

	@Inject
	private Logger logger;

	@Inject
	private Saml2DecoderService saml2DecoderService;
	
	@Inject
	private Saml2AssertionService saml2AssertionService;
	
	@Inject
	private SamlHelper samlHelper;
		
	@Inject 
	private SamlIdpMetadataService idpService;
	
	@Inject 
	private SamlSpConfigurationService spService;

	@Inject
	private ApplicationConfig appConfig;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		logger.debug("Consuming SAML AttributeQuery");
		
		try {
			AttributeQuery query = saml2DecoderService.decodeAttributeQuery(request);

			StatusCode statusCode = samlHelper.create(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
			statusCode.setValue(StatusCode.REQUEST_DENIED_URI);

			Status samlStatus = samlHelper.create(Status.class, Status.DEFAULT_ELEMENT_NAME);
			samlStatus.setStatusCode(statusCode);
			
			Response samlResponse = samlHelper.create(Response.class, Response.DEFAULT_ELEMENT_NAME);
			samlResponse.setStatus(samlStatus);
			
			XMLObjectBuilderFactory bf = Configuration.getBuilderFactory();
			Envelope envelope = (Envelope) bf.getBuilder(
					Envelope.DEFAULT_ELEMENT_NAME).buildObject(
					Envelope.DEFAULT_ELEMENT_NAME);
			Body body = (Body) bf.getBuilder(Body.DEFAULT_ELEMENT_NAME)
					.buildObject(Body.DEFAULT_ELEMENT_NAME);

			body.getUnknownXMLObjects().add(samlResponse);
			envelope.setBody(body);

			response.getWriter().print(samlHelper.marshal(envelope));
			
		} catch (MessageDecodingException e) {
			throw new ServletException("Authentication problem", e);
		} catch (SecurityException e) {
			throw new ServletException("Authentication problem", e);
		} catch (SamlAuthenticationException e) {
			throw new ServletException("Authentication problem", e);
		}
	}
	
	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public String getServletInfo() {
		return null;
	}

	@Override
	public void destroy() {
	}	
}
