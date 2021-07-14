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
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.EncryptedID;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.saml.saml2.encryption.EncryptedElementTypeEncryptedKeyResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.encryption.support.ChainingEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.InlineEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.SimpleKeyInfoReferenceEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.SimpleRetrievalMethodEncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.keyinfo.impl.StaticKeyInfoCredentialResolver;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.hook.UserUpdateHookException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.saml.exc.NoAssertionException;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;

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
	
	@Inject
	private ScriptingEnv scriptingEnv;
	
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
	
	public String resolveIdentifier(SamlIdentifier samlIdentifier, SamlIdpMetadataEntity idpEntity, StringBuffer debugLog)
			throws SamlAuthenticationException {
		if (idpEntity.getGenericStore().containsKey("extract_id_script")) {
			String scriptName = idpEntity.getGenericStore().get("extract_id_script");

			ScriptEntity scriptEntity = scriptingEnv.getScriptDao().findByName(scriptName);
			
			if (scriptEntity != null && scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine != null) {
					try {
						engine.eval(scriptEntity.getScript());
						
						Invocable invocable = (Invocable) engine;
						Object o = invocable.invokeFunction("extractId", scriptingEnv, samlIdentifier, logger, debugLog);
						if (o != null) {
							return o.toString();
						}
					} catch (NoSuchMethodException | ScriptException e) {
						logger.warn("Script execution failed", e);
					}
				}
			}
		}
		
		/*
		 * prefer subject-id over pairwise-id over persistent
		 */
		if (samlIdentifier.getSubjectId() != null) {
			return samlIdentifier.getSubjectId();
		}
		else if (samlIdentifier.getPairwiseId() != null) {
			return samlIdentifier.getPairwiseId();
		}
		else if (samlIdentifier.getPersistentId() != null) {
			return samlIdentifier.getPersistentId();
		}
		else {
			throw new SamlAuthenticationException("No usable identifier found. Acceptable identifiers are Pairwise-ID, Subject-ID or Persistent ID");
		}		
	}
	
	public SamlIdentifier extractPersistentId(SamlIdpMetadataEntity idpEntity, Assertion assertion, SamlSpConfigurationEntity spEntity, StringBuffer debugLog) 
			throws IOException, DecryptionException, SamlAuthenticationException {
		logger.debug("Fetching name Id from assertion");
		String persistentId = null;
		String pairwiseId = null;
		String subjectId = null;
		String transientId = null;
		
		logger.debug("Looking up pairwise ID\n");
		
		for (AttributeStatement as : assertion.getAttributeStatements()) {
			for (Attribute attribute : as.getAttributes()) {
				if (debugLog != null)
					debugLog.append("Check " + attribute.getName());
				
				if (attribute.getName().equals("urn:oasis:names:tc:SAML:attribute:pairwise-id")) {
					pairwiseId = getIdFromAttribute(idpEntity, attribute, debugLog);
				}
				else if (attribute.getName().equals("urn:oasis:names:tc:SAML:attribute:subject-id")) {
					subjectId = getIdFromAttribute(idpEntity, attribute, debugLog);
				}

				if (debugLog != null)
					debugLog.append("\n");
			}
			
			if (debugLog != null && as.getEncryptedAttributes() != null && as.getEncryptedAttributes().size() > 0) {
				debugLog.append("EncryptedAttributes are not supported\n");
			}
		}
		
		if (pairwiseId != null) {
			if (debugLog != null)
				debugLog.append("\nResulting pairwise ID: " + pairwiseId + "\n\n");
		}
		
		/*
		 * Assertion needs a Subject and a NameID, encrypted or not
		 */
		if (assertion.getSubject() == null) {
			if (debugLog != null)
				debugLog.append("Subject in Assertion is missing. Cannot process assertion without subject.\n");
			throw new SamlAuthenticationException("No Subject in assertion!");
		}
		
		if (assertion.getSubject().getNameID() == null &&
				assertion.getSubject().getEncryptedID() == null) {
			if (debugLog != null)
				debugLog.append("NameID in Assertion/Subject is missing. Cannot process assertion without NameID.\n");
			throw new SamlAuthenticationException("SAML2 NameID is missing.");
		}

		/*
		 * If the NameID is encrypted, decrypt it
		 */
		NameID nid;
		if (assertion.getSubject().getEncryptedID() != null) {
			if (debugLog != null)
				debugLog.append("NameID is encrypted Decrypting NameID...\n");

			EncryptedID eid = assertion.getSubject().getEncryptedID();
			SAMLObject samlObject = decryptNameID(eid, spEntity.getCertificate(), spEntity.getPrivateKey(), 
					spEntity.getStandbyCertificate(), spEntity.getStandbyPrivateKey());
			
			if (samlObject instanceof NameID)
				nid = (NameID) samlObject;
			else {
				if (debugLog != null)
					debugLog.append("Only Encrypted NameIDs are supoorted. Encrypted BaseIDs or embedded Assertions are not supported.\n")
						.append("NameID Type is: ").append(samlObject.getClass().getName()).append("\n");
				throw new SamlAuthenticationException("Only Encrypted NameIDs are supoorted. Encrypted BaseIDs or embedded Assertions are not supported");
			}
		}
		else
			nid = assertion.getSubject().getNameID();
		
		if (debugLog != null)
			debugLog.append("Resulting NameID (XML):\n").append(samlHelper.prettyPrint(nid)).append("\n");
		
		logger.debug("NameId format {} value {}", nid.getFormat(), nid.getValue());
		
		if (nid.getFormat().equals(NameID.TRANSIENT)) {
			transientId = nid.getValue();
		}
		else if (nid.getFormat().equals(NameID.PERSISTENT)) {
			persistentId = nid.getValue();
		}
		else {
			if (debugLog != null)
				debugLog.append("NameID is Unknown type (").append(nid.getClass().getName())
					.append("). At the moment, only Persistent NameID is supported\n");
			throw new SamlAuthenticationException("Unsupported SAML2 NameID Type");
		}

		return new SamlIdentifier(persistentId, pairwiseId, subjectId, transientId);
	}
	
	protected String getIdFromAttribute(SamlIdpMetadataEntity idpEntity, Attribute attribute, StringBuffer debugLog) {
		
		String id = null;
		
		if (debugLog != null) {
			debugLog.append(" ...found:\n");
			debugLog.append(samlHelper.prettyPrint(attribute) + "\n");
		}
		
		List<Object> attributeList = samlHelper.getAttribute(attribute);
		if (attributeList.size() == 1) {
			Object o = attributeList.get(0);
			if (o.toString().contains("@")) {
				String[] s = o.toString().split("@");
				if (s.length != 2 && debugLog != null) 
					debugLog.append("Pairwise ID contains more than one '@'\n");
				else {
					String p = s[0];
					String scope = s[1];
					boolean scopeMatch = false;
					for (SamlIdpScopeEntity idpScope : idpEntity.getScopes()) {
						if (idpScope.getRegex()) {
							if (scope.matches(idpScope.getScope())) {
								scopeMatch = true;
								break;
							}
						}
						else {
							if (idpScope.getScope().equals(scope)) {
								scopeMatch = true;
								break;
							}
						}
					}
					if ((! scopeMatch) && debugLog != null) {
						debugLog.append("Scope " + scope + " is NOT matching IDP " + idpEntity.getEntityId() + "\n");
						debugLog.append("Not using pairwise ID!\n");
					}
					else {
						if (debugLog != null)
							debugLog.append("Scope " + scope + " is matching IDP " + idpEntity.getEntityId() + "\n");
						id = p;
					}
				}
			}
			else {
				if (debugLog != null) 
					debugLog.append("Pairwise ID is not scoped. Can't use it. Looking for persistent ID.\n");
			}
		}
		else {
			if (debugLog != null) 
				debugLog.append("Pairwise ID does not contain exactly one value. Can't use it. Looking for persistent ID.\n");
		}
		
		return id;
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
		final List<EncryptedKeyResolver> list = new ArrayList<>();
		list.add(new InlineEncryptedKeyResolver());
		list.add(new EncryptedElementTypeEncryptedKeyResolver());
		list.add(new SimpleRetrievalMethodEncryptedKeyResolver());
		list.add(new SimpleKeyInfoReferenceEncryptedKeyResolver());
		ChainingEncryptedKeyResolver encryptionKeyResolver = new ChainingEncryptedKeyResolver(list);
		// At this point, we have some missing methods to get the encrypted Key out of the xml
		// This seems to be necessary with some IDPs
		//InlineEncryptedKeyResolver encryptionKeyResolver = new InlineEncryptedKeyResolver();
		Decrypter decrypter = new Decrypter(null, keyResolver, encryptionKeyResolver);
		decrypter.setRootInNewDocument(true);
		return decrypter;
	}
}
