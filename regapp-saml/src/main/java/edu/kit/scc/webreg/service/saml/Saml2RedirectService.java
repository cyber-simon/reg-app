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

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.app.VelocityEngine;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
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

import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

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

	@Inject
	private CryptoHelper cryptoHelper;

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

		if (idpEntity.getGenericStore().containsKey("sign_authnrequest")
				&& idpEntity.getGenericStore().get("sign_authnrequest").equalsIgnoreCase("true")) {
			try {
				signAuthRequest(spEntity, messageContext);
			} catch (SamlAuthenticationException e) {
				logger.warn("Cannot sign message, sending unsigned", e);
			}
		}

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
			// encoder.setVelocityTemplateId("templates/saml2-post-binding.vm");
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

	private void signAuthRequest(SamlSpConfigurationEntity spConfig, MessageContext<SAMLObject> messageContext)
			throws SamlAuthenticationException {
		SecurityParametersContext securityContext = buildSecurityContext(spConfig);
		messageContext.addSubcontext(securityContext);
		try {
			SAMLMessageSecuritySupport.signMessage(messageContext);
		} catch (SecurityException | MarshallingException | SignatureException e) {
			throw new SamlAuthenticationException("Cannot sign message", e);
		}
	}

	private SecurityParametersContext buildSecurityContext(SamlSpConfigurationEntity spConfig)
			throws SamlAuthenticationException {
		PrivateKey privateKey;
		X509Certificate publicKey;
		try {
			privateKey = cryptoHelper.getPrivateKey(spConfig.getPrivateKey());
			publicKey = cryptoHelper.getCertificate(spConfig.getCertificate());
		} catch (IOException e) {
			throw new SamlAuthenticationException("Private key is not set up properly", e);
		}

		BasicX509Credential credential = new BasicX509Credential(publicKey, privateKey);
		List<Credential> credentialList = new ArrayList<Credential>();
		credentialList.add(credential);

		BasicSignatureSigningConfiguration ssConfig = DefaultSecurityConfigurationBootstrap
				.buildDefaultSignatureSigningConfiguration();
		ssConfig.setSigningCredentials(credentialList);
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new SignatureSigningConfigurationCriterion(ssConfig));
		SAMLMetadataSignatureSigningParametersResolver smsspr = new SAMLMetadataSignatureSigningParametersResolver();

		SignatureSigningParameters ssp;
		try {
			ssp = smsspr.resolveSingle(criteriaSet);
		} catch (ResolverException e) {
			throw new SamlAuthenticationException(e);
		}
		logger.debug("Resolved algo {} for signing", ssp.getSignatureAlgorithm());
		SecurityParametersContext securityContext = new SecurityParametersContext();
		securityContext.setSignatureSigningParameters(ssp);

		return securityContext;
	}
}
