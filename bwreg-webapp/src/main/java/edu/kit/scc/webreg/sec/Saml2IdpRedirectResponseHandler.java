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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Condition;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.SamlAAConfigurationService;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.Saml2DecoderService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SamlIdpService;
import edu.kit.scc.webreg.service.saml.SsoHelper;
import edu.kit.scc.webreg.session.SessionManager;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

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
	private CryptoHelper cryptoHelper;
	
	@Inject
	private UserService userService;
	
	@Inject
	private SamlAAConfigurationService aaConfigService;
	
	@Inject
	private ApplicationConfig appConfig;
	
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (session.getAuthnRequestId() == null) {
			logger.warn("No AuthnRequestId set in session. Cannot continue");
			return;
		}
		
		if (session.getUserId() == null) {
			logger.warn("No UserId set in session. Cannot continue");
			return;
		}
		
		SamlAAConfigurationEntity aaConfig = aaConfigService.findByEntityId("https://bwidm.scc.kit.edu/attribute-authority");
		
		UserEntity user = userService.findById(session.getUserId());
		
		AuthnRequest authnRequest = samlIdpService.resumeAuthnRequest(session.getAuthnRequestId());
		
		logger.debug("Authn request reloaded: {}", samlHelper.prettyPrint(authnRequest));
		
		Response samlResponse = ssoHelper.buildAuthnResponse(authnRequest, "https://bwidm.scc.kit.edu/saml/idp/metadata");

		Audience audience = samlHelper.create(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
		audience.setAudienceURI("https://bwidm-dev.scc.kit.edu/nextcloud/index.php/apps/user_saml/saml/metadata");
		AudienceRestriction ar = samlHelper.create(AudienceRestriction.class, AudienceRestriction.DEFAULT_ELEMENT_NAME);
		ar.getAudiences().add(audience);
		
		Conditions conditions = samlHelper.create(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
		conditions.setNotBefore(new DateTime());
		conditions.setNotOnOrAfter(new DateTime(System.currentTimeMillis() + (5L * 60L * 1000L)));
		conditions.getAudienceRestrictions().add(ar);
		
		AuthnContextClassRef accr = samlHelper.create(AuthnContextClassRef.class, AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
		AuthnContext ac = samlHelper.create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
		ac.setAuthnContextClassRef(accr);
		AuthnStatement as = samlHelper.create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
		as.setAuthnContext(ac);
		as.setAuthnInstant(new DateTime());
		as.setSessionNotOnOrAfter(new DateTime(System.currentTimeMillis() + (5L * 60L * 1000L)));
		as.setSessionIndex(samlHelper.getRandomId());
		
		Assertion assertion = samlHelper.create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setID(samlHelper.getRandomId());
		assertion.setIssueInstant(new DateTime());
		assertion.setIssuer(ssoHelper.buildIssuser("https://bwidm.scc.kit.edu/saml/idp/metadata"));
		assertion.setSubject(ssoHelper.buildSubject(samlHelper.getRandomId(), NameID.TRANSIENT, authnRequest.getID()));
		assertion.setConditions(conditions);
		assertion.getAttributeStatements().add(buildAttributeStatement(user));
		assertion.getAuthnStatements().add(as);
		samlResponse.getAssertions().add(assertion);

		PrivateKey privateKey;
		X509Certificate publicKey;
		try {
			privateKey = cryptoHelper.getPrivateKey(aaConfig.getPrivateKey());
			publicKey = cryptoHelper.getCertificate(aaConfig.getCertificate());
		} catch (IOException e) {
			throw new ServletException("Private key is not set up properly", e);
		}

		BasicX509Credential credential = new BasicX509Credential(publicKey, privateKey);
		List<Credential> credentialList = new ArrayList<Credential>();
		credentialList.add(credential);
		
		BasicSignatureSigningConfiguration ssConfig = DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();
		ssConfig.setSigningCredentials(credentialList);
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new SignatureSigningConfigurationCriterion(ssConfig));
		SAMLMetadataSignatureSigningParametersResolver smsspr = new SAMLMetadataSignatureSigningParametersResolver();

		SignatureSigningParameters ssp;
		try {
			ssp = smsspr.resolveSingle(criteriaSet);
		} catch (ResolverException e) {
			throw new ServletException(e);
		}
		logger.debug("Resolved algo {} for signing", ssp.getSignatureAlgorithm());
		SecurityParametersContext securityContext = new SecurityParametersContext();
		securityContext.setSignatureSigningParameters(ssp);
		
		HTTPPostEncoder postEncoder = new HTTPPostEncoder();
		postEncoder.setHttpServletResponse(response);
		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();
		messageContext.setMessage(samlResponse);

		messageContext.addSubcontext(securityContext);

		SAMLPeerEntityContext entityContext = new SAMLPeerEntityContext();
		SAMLEndpointContext endpointContext = new SAMLEndpointContext();
		
		AssertionConsumerService acs = samlHelper.create(AssertionConsumerService.class, AssertionConsumerService.DEFAULT_ELEMENT_NAME);
		acs.setLocation(authnRequest.getAssertionConsumerServiceURL());
		endpointContext.setEndpoint(acs);
		entityContext.addSubcontext(endpointContext);
		messageContext.addSubcontext(entityContext);

		try {
			SAMLMessageSecuritySupport.signMessage(messageContext);
		} catch (SecurityException | MarshallingException | SignatureException e) {
			throw new ServletException(e);
		}
		
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
	
	private AttributeStatement buildAttributeStatement(UserEntity user) {
		AttributeStatement attributeStatement = samlHelper.create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);
		attributeStatement.getAttributes().add(buildAttribute(
				"urn:oid:1.3.6.1.4.1.5923.1.1.1.6", "eduPersonPrincipalName", Attribute.URI_REFERENCE, user.getEppn()));
		attributeStatement.getAttributes().add(buildAttribute(
				"urn:oid:0.9.2342.19200300.100.1.3", "mail", Attribute.URI_REFERENCE, user.getEmail()));
		
		return attributeStatement;
	}
	
	private Attribute buildAttribute(String name, String friendlyName, String nameFormat, String... values) {
		Attribute attribute = samlHelper.create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
		attribute.setName(name);
		attribute.setFriendlyName(friendlyName);
		attribute.setNameFormat(nameFormat);
		
		for (String value : values) {
			XSString xsany = samlHelper.create(XSString.class, XSString.TYPE_NAME, AttributeValue.DEFAULT_ELEMENT_NAME);
			xsany.setValue(value);
			attribute.getAttributeValues().add(xsany);
		}
		
		return attribute;
	}	
	
}
