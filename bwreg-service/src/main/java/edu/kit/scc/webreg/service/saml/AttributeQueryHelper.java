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
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder;

import org.joda.time.DateTime;
import org.opensaml.core.config.Configuration;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.metadata.AttributeService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.soap.common.SOAPException;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.opensaml.xmlsec.DecryptionConfiguration;
import org.opensaml.xmlsec.SignatureSigningConfiguration;
import org.opensaml.xmlsec.keyinfo.KeyInfoGenerator;
import org.opensaml.xmlsec.keyinfo.impl.X509KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.MetadataException;

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

	public Response query(String persistentId, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity) throws MetadataException, SOAPException, SecurityException {
		AttributeService attributeService = metadataHelper.getAttributeService(idpEntityDescriptor);
		if (attributeService == null || attributeService.getLocation() == null)
			throw new MetadataException("No Attribute Service found for IDP " + idpEntity.getEntityId());
			
		AttributeQuery attrQuery = buildAttributeQuery(
				persistentId, spEntity.getEntityId());
		Envelope envelope = buildSOAP11Envelope(attrQuery);
		
		BasicSOAPMessageContext soapContext = new BasicSOAPMessageContext();
		soapContext.setOutboundMessage(envelope);
		
		BasicX509Credential signingCredential;
		X509Certificate x509Cert;
		PrivateKey privateKey;
		
		try {
			x509Cert = cryptoHelper.getCertificate(spEntity.getCertificate());
			privateKey = cryptoHelper.getPrivateKey(spEntity.getPrivateKey());
			signingCredential = SecurityHelper.getSimpleCredential(x509Cert, privateKey);
		} catch (IOException e1) {
			throw new MetadataException("No signing credential for SP " + spEntity.getEntityId(), e1);
		}

		HttpClientBuilder clientBuilder = new HttpClientBuilder();
/*		
		try {
			clientBuilder.setHttpsProtocolSocketFactory(new CustomSecureProtocolSocketFactory(x509Cert, privateKey));
		} catch (KeyManagementException e) {
			logger.info("Cannot spawn CustomSecureProtocolSocketFactory: {}", e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			logger.info("Cannot spawn CustomSecureProtocolSocketFactory: {}", e.getMessage());
		}
*/		
		Signature signature = (Signature) samlHelper.getBuilderFactory()
            	.getBuilder(Signature.DEFAULT_ELEMENT_NAME)
            	.buildObject(Signature.DEFAULT_ELEMENT_NAME);
		
		SignatureSigningConfiguration ssc = ConfigurationService.get(SignatureSigningConfiguration.class);
		
		X509KeyInfoGeneratorFactory keyInfoFac = (X509KeyInfoGeneratorFactory) ssc
				.getKeyInfoGeneratorManager().getDefaultManager()
				.getFactory(signingCredential);
		keyInfoFac.setEmitEntityCertificate(false);
		keyInfoFac.setEmitEntityCertificateChain(false);
		KeyInfoGenerator keyInfoGen = keyInfoFac.newInstance();
		KeyInfo keyInfo;
		try {
			keyInfo = keyInfoGen.generate(signingCredential);
		} catch (SecurityException e1) {
			throw new MetadataException("Cannot generate keyInfo for SP " + spEntity.getEntityId(), e1);
		}

		signature.setSigningCredential(signingCredential);
		signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
		signature.setCanonicalizationAlgorithm(SignatureConstants.TRANSFORM_C14N_EXCL_WITH_COMMENTS);
		signature.setKeyInfo(keyInfo);

		attrQuery.setSignature(signature);
		
		HttpSignableSoapClient soapClient = new HttpSignableSoapClient(
				clientBuilder.buildClient(), samlHelper.getBasicParserPool(),
				signature);

		soapClient.send(attributeService.getLocation(), soapContext);
		
		Envelope returnEnvelope = (Envelope) soapContext.getInboundMessage();

		return getResponseFromEnvelope(returnEnvelope);
	}
		
	public Response query(UserEntity entity, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity) throws MetadataException, SOAPException, SecurityException {
		return query(entity.getPersistentId(), idpEntity, idpEntityDescriptor, spEntity);	
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

	public Envelope buildSOAP11Envelope(XMLObject payload) {
		XMLObjectBuilderFactory bf = samlHelper.getBuilderFactory();
		Envelope envelope = (Envelope) bf.getBuilder(
				Envelope.DEFAULT_ELEMENT_NAME).buildObject(
				Envelope.DEFAULT_ELEMENT_NAME);
		Body body = (Body) bf.getBuilder(Body.DEFAULT_ELEMENT_NAME)
				.buildObject(Body.DEFAULT_ELEMENT_NAME);

		body.getUnknownXMLObjects().add(payload);
		envelope.setBody(body);

		return envelope;
	}

	public Subject createSubject(String persistentId) {
		NameID nameID = samlHelper.create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameID.setValue(persistentId);
		nameID.setFormat(NameID.PERSISTENT);

		Subject subject = samlHelper.create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
		subject.setNameID(nameID);

		return subject;
	}

}
