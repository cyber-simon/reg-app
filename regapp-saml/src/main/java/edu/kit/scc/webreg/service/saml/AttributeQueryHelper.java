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
import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.joda.time.DateTime;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.InOutOperationContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.pipeline.httpclient.BasicHttpClientMessagePipeline;
import org.opensaml.messaging.pipeline.httpclient.HttpClientMessagePipeline;
import org.opensaml.messaging.pipeline.httpclient.HttpClientMessagePipelineFactory;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.binding.decoding.impl.HttpClientResponseSOAP11Decoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HttpClientRequestSOAP11Encoder;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.AttributeService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.soap.client.http.PipelineFactoryHttpSOAPClient;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.service.saml.exc.MetadataException;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

@Named("attributeQueryHelper")
@ApplicationScoped
public class AttributeQueryHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private SamlHelper samlHelper;

	@Inject
	private MetadataHelper metadataHelper;
	
	@Inject 
	private CryptoHelper cryptoHelper;
	
	@Inject
	ApplicationConfig appConfig;

	public Response query(String persistentId, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity, StringBuffer debugLog) throws Exception {
		
		if (debugLog != null) {
			debugLog.append("Starting attribute query for")
				.append(persistentId).append(" idp: ").append(idpEntity.getEntityId()).append(" sp: ")
				.append(spEntity.getEntityId()).append("\n");
		}
		
		AttributeService attributeService = metadataHelper.getAttributeService(idpEntityDescriptor);
		if (attributeService == null || attributeService.getLocation() == null)
			throw new MetadataException("No Attribute Service found for IDP " + idpEntity.getEntityId());
			
		AttributeQuery attrQuery = buildAttributeQuery(
				persistentId, spEntity.getEntityId());
		
		MessageContext<SAMLObject> inbound = new MessageContext<SAMLObject>();
		MessageContext<SAMLObject> outbound = new MessageContext<SAMLObject>();
		outbound.setMessage(attrQuery);

		SAMLPeerEntityContext entityContext = new SAMLPeerEntityContext();
		entityContext.setEntityId(idpEntity.getEntityId());
		SAMLEndpointContext endpointContext = new SAMLEndpointContext();
		endpointContext.setEndpoint(attributeService);
		entityContext.addSubcontext(endpointContext);
		outbound.addSubcontext(entityContext);

		SAMLBindingContext bindingContext = new SAMLBindingContext();
		bindingContext.setHasBindingSignature(true);
		outbound.addSubcontext(bindingContext);
		
		SOAP11Context soapContext = new SOAP11Context();
		outbound.addSubcontext(soapContext);

		PrivateKey privateKey;
		X509Certificate publicKey;
		try {
			privateKey = cryptoHelper.getPrivateKey(spEntity.getPrivateKey());
			publicKey = cryptoHelper.getCertificate(spEntity.getCertificate());
		} catch (IOException e) {
			throw new SamlAuthenticationException("Private key is not set up properly", e);
		}

		BasicX509Credential credential = new BasicX509Credential(publicKey, privateKey);
		List<Credential> credentialList = new ArrayList<Credential>();
		credentialList.add(credential);
		
		BasicSignatureSigningConfiguration ssConfig = DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();
		ssConfig.setSigningCredentials(credentialList);
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new SignatureSigningConfigurationCriterion(ssConfig));
		criteriaSet.add(new RoleDescriptorCriterion(idpEntityDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS)));
		SAMLMetadataSignatureSigningParametersResolver smsspr = new SAMLMetadataSignatureSigningParametersResolver();
		
		SignatureSigningParameters ssp = smsspr.resolveSingle(criteriaSet);
		logger.debug("Resolved algo {} for signing", ssp.getSignatureAlgorithm());
		SecurityParametersContext securityContext = new SecurityParametersContext();
		securityContext.setSignatureSigningParameters(ssp);
		outbound.addSubcontext(securityContext);
		
		InOutOperationContext<SAMLObject, SAMLObject> inOutContext =
				new InOutOperationContext<SAMLObject, SAMLObject>(inbound, outbound);
		
		if (debugLog != null) {
			debugLog.append("\nOutgoing SAML Message before signing:\n\n")
				.append(samlHelper.prettyPrint(outbound.getMessage()))
				.append("\n");
		}
		
		SAMLMessageSecuritySupport.signMessage(outbound);

		if (debugLog != null) {
			debugLog.append("\nOutgoing SAML Message after signing:\n\n")
				.append(samlHelper.prettyPrint(outbound.getMessage()))
				.append("\n");
		}

		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(getSocketTimeout())
				.setConnectTimeout(getConnectTimeout())
				.setConnectionRequestTimeout(getConnectionRequestTimeout())
				.build();
		CloseableHttpClient client = HttpClients.custom()
				.setSSLSocketFactory(getSSLConnectionSocketFactory(idpEntityDescriptor))
				.setDefaultRequestConfig(requestConfig)
				.build();

		PipelineFactoryHttpSOAPClient<SAMLObject, SAMLObject> pf = new PipelineFactoryHttpSOAPClient<SAMLObject, SAMLObject>();
		pf.setHttpClient(client);
		pf.setPipelineFactory(new HttpClientMessagePipelineFactory<SAMLObject, SAMLObject>() {
			
			@Override
			public HttpClientMessagePipeline<SAMLObject, SAMLObject> newInstance(
					String pipelineName) {
				return new BasicHttpClientMessagePipeline<SAMLObject, SAMLObject>(new HttpClientRequestSOAP11Encoder(), new HttpClientResponseSOAP11Decoder());
			}
			
			@Override
			public HttpClientMessagePipeline<SAMLObject, SAMLObject> newInstance() {
				return new BasicHttpClientMessagePipeline<SAMLObject, SAMLObject>(new HttpClientRequestSOAP11Encoder(), new HttpClientResponseSOAP11Decoder());
			}
		});
		
		try {
			pf.send(attributeService.getLocation(), inOutContext);
			
			Response returnResponse = (Response) inOutContext.getInboundMessageContext().getMessage();
			
			return returnResponse;
		}
		finally {
			client.close();
		}
	}

	public Response query(String persistentId, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity) throws Exception {
		return query(persistentId, idpEntity, idpEntityDescriptor, spEntity, null);	
	}
	
	public Response query(SamlUserEntity entity, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity) throws Exception {
		return query(entity.getPersistentId(), idpEntity, idpEntityDescriptor, spEntity, null);	
	}
	
	public Response query(SamlUserEntity entity, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity, StringBuffer debugLog) throws Exception {
		return query(entity.getPersistentId(), idpEntity, idpEntityDescriptor, spEntity, debugLog);	
	}
	
	public Response getResponseFromEnvelope(Envelope envelope) {
		Body body = envelope.getBody();
		List<XMLObject> xmlObjects = body.getUnknownXMLObjects();

		Response response = (Response) xmlObjects.get(0);
		
		return response;
	}
	
	public AttributeQuery buildAttributeQuery(String persistentId, String issuerEntityId) {
		AttributeQuery attrQuery = samlHelper.create(AttributeQuery.class, AttributeQuery.DEFAULT_ELEMENT_NAME);
		attrQuery.setID(samlHelper.getRandomId());
		attrQuery.setSubject(createSubject(persistentId));
		attrQuery.setVersion(SAMLVersion.VERSION_20);
		attrQuery.setIssueInstant(new DateTime());

		Issuer issuer = samlHelper.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(issuerEntityId);
		attrQuery.setIssuer(issuer);
		return attrQuery;
	}

	public Subject createSubject(String persistentId) {
		NameID nameID = samlHelper.create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameID.setValue(persistentId);
		nameID.setFormat(NameID.PERSISTENT);

		Subject subject = samlHelper.create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
		subject.setNameID(nameID);

		return subject;
	}

	private int getConnectTimeout() {
		String aqString = appConfig.getConfigValue("attributequery_timeout");
		if (aqString == null)
			return 30*1000;
		else 
			return Integer.parseInt(aqString);
	}

	private int getSocketTimeout() {
		String aqString = appConfig.getConfigValue("attributequery_socket_timeout");
		if (aqString == null)
			return 30*1000;
		else 
			return Integer.parseInt(aqString);
	}

	private int getConnectionRequestTimeout() {
		String aqString = appConfig.getConfigValue("attributequery_connectionrequest_timeout");
		if (aqString == null)
			return 30*1000;
		else 
			return Integer.parseInt(aqString);
	}
	
	private SSLConnectionSocketFactory getSSLConnectionSocketFactory(EntityDescriptor idpEntityDescriptor) throws KeyManagementException, NoSuchAlgorithmException {
		String proto = appConfig.getConfigValue("attributequery_tls_version");
		String[] protos;
		if (proto == null)
			protos = new String[] { "TLSv1.2" };
		else 
			protos = proto.split(",");

		DOMMetadataResolver mp = new DOMMetadataResolver(idpEntityDescriptor.getDOM());
		mp.setId(idpEntityDescriptor.getEntityID() + "-resolver");
		
		PredicateRoleDescriptorResolver roleResolver = new PredicateRoleDescriptorResolver(mp);
		KeyInfoCredentialResolver keyInfoCredResolver = DefaultSecurityConfigurationBootstrap.buildBasicInlineKeyInfoCredentialResolver();

		MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver();
		mdCredResolver.setKeyInfoCredentialResolver(keyInfoCredResolver);
		mdCredResolver.setRoleDescriptorResolver(roleResolver);
		try {
			mp.initialize();
			roleResolver.initialize();
			mdCredResolver.initialize();
		} catch (ComponentInitializationException e) {
			logger.error("Cannot init MDCredResolver", e);
			throw new IllegalStateException("Cannot init MDCredResolver", e);
		}

		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIdCriterion(idpEntityDescriptor.getEntityID()));
		criteriaSet.add(new EntityRoleCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
		criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
		
		Set<PublicKey> keySet = new HashSet<PublicKey>();
		
		try {
			Iterable<Credential> credentials = mdCredResolver.resolve(criteriaSet);
			for (Credential credential : credentials) {
				keySet.add(credential.getPublicKey());
			}
		} catch (ResolverException e1) {
			logger.warn("Exception", e1);
		}
		
		SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
		try {
			sslContextBuilder.loadTrustMaterial(null, new AttributeQueryTrustStrategy(keySet));
		} catch (NoSuchAlgorithmException | KeyStoreException e) {
			logger.warn("Exception", e);
		}
		SSLContext sslContext = sslContextBuilder.build();

		SSLConnectionSocketFactory f = new SSLConnectionSocketFactory(sslContext, protos,
				null, new DefaultHostnameVerifier());
		return f;
	}
}
