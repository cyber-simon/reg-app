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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.EncryptedID;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.exc.NoAssertionException;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;

@ApplicationScoped
public class Saml2AssertionService {

	@Inject
	private Logger logger;
	
	@Inject
	private CryptoHelper cryptoHelper;
	
	@Inject
	private SamlHelper samlHelper;
	
	@Inject
	private Saml2ResponseValidationService saml2ValidationService;
	
	public Assertion processSamlResponse(Response samlResponse, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity) 
					throws IOException, DecryptionException, SamlAuthenticationException {
		return processSamlResponse(samlResponse, idpEntity, idpEntityDescriptor, spEntity, true);
	}
	
	public Assertion processSamlResponse(Response samlResponse, SamlMetadataEntity idpEntity, 
			EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity spEntity, boolean checkSignature) 
					throws IOException, DecryptionException, SamlAuthenticationException {
		saml2ValidationService.verifyStatus(samlResponse);
		saml2ValidationService.verifyIssuer(idpEntity, samlResponse);
		saml2ValidationService.verifyExpiration(samlResponse, 1000L * 60L * 10L);

		Boolean responseSignatureValid = false;

		if (checkSignature) {
			try {
				logger.debug("Validating SamlResponse Signature for " + samlResponse.getID());					
				saml2ValidationService.validateIdpSignature(samlResponse, samlResponse.getIssuer(), idpEntityDescriptor);
				logger.debug("Validating SamlResponse Signature success for " + samlResponse.getID());					
				responseSignatureValid = true;
			} catch (SamlAuthenticationException e) {
				logger.debug("SamlResponse doesn't contain a signature");
			}
		}
		else {
			logger.debug("Skipping signature check for SamlResponse");
		}

		List<Assertion> assertionList = samlResponse.getAssertions();
		List<EncryptedAssertion> encryptedAssertionList = samlResponse.getEncryptedAssertions();
		logger.debug("Got {} assertion and {} encrypted assertion", assertionList.size(), encryptedAssertionList.size());

		Assertion assertion;
		
		/**
		 * take first encrypted assertion, then first assertion, ignore all other
		 */
		if (encryptedAssertionList.size() > 0) {
			assertion = decryptAssertion(
					encryptedAssertionList.get(0), spEntity.getCertificate(), spEntity.getPrivateKey(), 
					spEntity.getStandbyCertificate(), spEntity.getStandbyPrivateKey());
		}
		else if (assertionList.size() > 0) {
			assertion = assertionList.get(0);
		}
		else {
			throw new NoAssertionException("SAML2 Response contained no Assertion");
		}

		if (checkSignature) {
			if (! responseSignatureValid) {
				logger.debug("Validating Assertion Signature for " + assertion.getID());					
				saml2ValidationService.validateIdpSignature(assertion, assertion.getIssuer(), idpEntityDescriptor);
				logger.debug("Validating Assertion Signature success for " + assertion.getID());
			}
			else {
				logger.debug("Skipping assertion signature validation. SamlResponse was signed");
			}
		}
		else {
			logger.debug("Skipping signature check for Assertion");
		}
		
		return assertion;
	}
	
	public String extractPersistentId(Assertion assertion, SamlSpConfigurationEntity spEntity) 
			throws IOException, DecryptionException, SamlAuthenticationException {
		logger.debug("Fetching name Id from assertion");
		String persistentId;
		
		/*
		 * Assertion needs a Subject and a NameID, encrypted or not
		 */
		if (assertion.getSubject() == null)
			throw new SamlAuthenticationException("No Subject in assertion!");
		
		if (assertion.getSubject().getNameID() == null &&
				assertion.getSubject().getEncryptedID() == null)
			throw new SamlAuthenticationException("SAML2 NameID is missing.");

		/*
		 * If the NameID is encrypted, decrypt it
		 */
		NameID nid;
		if (assertion.getSubject().getEncryptedID() != null) {
			EncryptedID eid = assertion.getSubject().getEncryptedID();
			SAMLObject samlObject = decryptNameID(eid, spEntity.getCertificate(), spEntity.getPrivateKey(), 
					spEntity.getStandbyCertificate(), spEntity.getStandbyPrivateKey());
			
			if (samlObject instanceof NameID)
				nid = (NameID) samlObject;
			else
				throw new SamlAuthenticationException("Only Encrypted NameIDs are supoorted. Encrypted BaseIDs or embedded Assertions are not supported");
		}
		else
			nid = assertion.getSubject().getNameID();
		
		logger.debug("NameId format {} value {}", nid.getFormat(), nid.getValue());
		if (nid.getFormat().equals(NameID.TRANSIENT)) {
			throw new SamlAuthenticationException("NameID is Transient but must be Persistent");
		}
		else if (nid.getFormat().equals(NameID.PERSISTENT)) {
			persistentId = nid.getValue();
		}
		else
			throw new SamlAuthenticationException("Unsupported SAML2 NameID Type");

		return persistentId;
	}
	
	public Assertion decryptAssertion(EncryptedAssertion encryptedAssertion,
			String cert, String privateKey, String standbyCert, String standbyPrivateKey) throws IOException, DecryptionException, SamlAuthenticationException {
		logger.debug("Decrypting assertion...");
		
		Decrypter decrypter = buildDecrypter(cert, privateKey, standbyCert, standbyPrivateKey);
		Assertion assertion = decrypter.decrypt(encryptedAssertion);
		return assertion;
	}
	
	public SAMLObject decryptNameID(EncryptedID encryptedID,
			String cert, String privateKey, String standbyCert, String standbyPrivateKey) throws IOException, DecryptionException, SamlAuthenticationException {
		logger.debug("Decrypting nameID...");
		
		Decrypter decrypter = buildDecrypter(cert, privateKey, standbyCert, standbyPrivateKey);
		SAMLObject samlObject = decrypter.decrypt(encryptedID);
		return samlObject;
	}	
	
	public Map<String, List<Object>> extractAttributes(Assertion assertion) {
		if (assertion == null)
			return null;
		
		Map<String, Attribute> attributes = samlHelper.assertionToAttributeMap(assertion);
		Map<String, List<Object>> attributeMap = new HashMap<String, List<Object>>();
		
		for (Entry<String, Attribute> entry : attributes.entrySet()) {
			attributeMap.put(entry.getKey(), samlHelper.getAttribute(entry.getValue()));
		}
		
		return attributeMap;
	}
	
	private Decrypter buildDecrypter(String cert, String privateKey, String standbyCert, String standbyPrivateKey) 
			throws SamlAuthenticationException {
		PrivateKey pk;
		X509Certificate c;
		try {
			pk = cryptoHelper.getPrivateKey(privateKey);
			c = cryptoHelper.getCertificate(cert);
		} catch (IOException e) {
			throw new SamlAuthenticationException("Private key is not set up properly", e);
		}
		
		if (pk == null) {
			throw new SamlAuthenticationException("Private key is not set up properly (is null)");
		}
			
		List<Credential> decryptCredentialList = new ArrayList<Credential>();
		BasicX509Credential decryptCredential = new BasicX509Credential(c, pk);
		decryptCredentialList.add(decryptCredential);
		
		if (standbyPrivateKey != null && (! standbyPrivateKey.equals(""))) {
			try {
				PrivateKey spk = cryptoHelper.getPrivateKey(standbyPrivateKey);
				X509Certificate sc = cryptoHelper.getCertificate(standbyCert);
				BasicX509Credential standbyDecryptCredential = new BasicX509Credential(sc, spk);
				decryptCredentialList.add(standbyDecryptCredential);
			} catch (IOException e) {
				logger.warn("Standby private Key is not set up properly: {}. I won't use it", e.getMessage());
			}
		}
		
		KeyInfoCredentialResolver keyResolver = new StaticKeyInfoCredentialResolver(decryptCredentialList);
		InlineEncryptedKeyResolver encryptionKeyResolver = new InlineEncryptedKeyResolver();
		Decrypter decrypter = new Decrypter(null, keyResolver, encryptionKeyResolver);
		decrypter.setRootInNewDocument(true);
		return decrypter;
	}
}
