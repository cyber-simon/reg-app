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
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.metadata.AttributeService;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.ws.soap.client.BasicSOAPMessageContext;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.MetadataException;

@Named("attributeQueryHelper")
@ApplicationScoped
public class AttributeQueryHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private MetadataHelper metadataHelper;
	
	@Inject 
	private CryptoHelper cryptoHelper;

	public Response query(String persistentId, SamlIdpMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity) throws MetadataException, SOAPException, SecurityException {
		AttributeService attributeService = metadataHelper.getAttributeService(idpEntityDescriptor);
		if (attributeService == null || attributeService.getLocation() == null)
			throw new MetadataException("No Attribute Service found for IDP " + idpEntity.getEntityId());
			
		AttributeQuery attrQuery = buildAttributeQuery(
				persistentId, spEntity.getEntityId());
		Envelope envelope = buildSOAP11Envelope(attrQuery);
		BasicSOAPMessageContext soapContext = new BasicSOAPMessageContext();
		soapContext.setOutboundMessage(envelope);
		
		HttpClientBuilder clientBuilder = new HttpClientBuilder();
		
		BasicX509Credential signingCredential;
		try {
			signingCredential = SecurityHelper.getSimpleCredential(
					cryptoHelper.getCertificate(spEntity.getCertificate()), 
					cryptoHelper.getKeyPair(spEntity.getPrivateKey()).getPrivate());
		} catch (IOException e1) {
			throw new MetadataException("No signing credential for SP " + spEntity.getEntityId(), e1);
		}
		
		Signature signature = (Signature) Configuration.getBuilderFactory()
            	.getBuilder(Signature.DEFAULT_ELEMENT_NAME)
            	.buildObject(Signature.DEFAULT_ELEMENT_NAME);
		X509KeyInfoGeneratorFactory keyInfoFac = (X509KeyInfoGeneratorFactory) Configuration
				.getGlobalSecurityConfiguration()
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
		
	public Response query(UserEntity entity, SamlIdpMetadataEntity idpEntity, 
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
		XMLObjectBuilderFactory bf = Configuration.getBuilderFactory();
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
