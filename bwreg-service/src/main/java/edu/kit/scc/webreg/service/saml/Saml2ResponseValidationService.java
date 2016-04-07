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
import javax.xml.namespace.QName;

import org.joda.time.Duration;
import org.joda.time.Instant;
import org.opensaml.Configuration;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.AttributeQuery;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.security.MetadataCredentialResolver;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.criteria.UsageCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.exc.SamlUnknownPrincipalException;

@ApplicationScoped
public class Saml2ResponseValidationService {

	@Inject
	private Logger logger;
	
	@Inject
	private SamlHelper samlHelper;

	public void verifyIssuer(SamlMetadataEntity metadataEntity,
			Response samlResponse) throws SamlAuthenticationException {
		verifyIssuer(metadataEntity, samlResponse.getIssuer());
	}

	public void verifyIssuer(SamlMetadataEntity metadataEntity,
			AttributeQuery attributeQuery) throws SamlAuthenticationException {
		verifyIssuer(metadataEntity, attributeQuery.getIssuer());
	}

	public void verifyIssuer(SamlMetadataEntity metadataEntity,
			Issuer issuer) throws SamlAuthenticationException {

		if (issuer == null)
			throw new SamlAuthenticationException("Response issuer is not set");

		String issuerString = issuer.getValue();
		if (! issuerString.equals(metadataEntity.getEntityId())) 
			throw new SamlAuthenticationException("Response issuer " + issuerString + 
					" differs from excpected " + metadataEntity.getEntityId());

	}

	public void verifyExpiration(Response samlResponse, Long expiryMillis) 
			throws SamlAuthenticationException {

		Duration duration = new Duration(samlResponse.getIssueInstant(), new Instant());
		if (duration.isLongerThan(new Duration(expiryMillis))) 
			throw new SamlAuthenticationException("Response is already expired after " + duration.getStandardSeconds() + " seconds");
	}	

	public void verifyStatus(Response samlResponse) 
			throws SamlAuthenticationException {

		if (samlResponse.getStatus() == null || samlResponse.getStatus().getStatusCode() == null)
			throw new SamlAuthenticationException("SAML Response does not contain a status code");
			
		Status status = samlResponse.getStatus();
		if (status.getStatusCode().getStatusCode() != null &&
				StatusCode.UNKNOWN_PRINCIPAL_URI.equals(status.getStatusCode().getStatusCode().getValue())) {
			String s = samlHelper.prettyPrint(status);
			logger.info("SAML Response Status: {}", s);
			throw new SamlUnknownPrincipalException("SAML Response: Unknown Principal " + status.getStatusCode().getValue());
		}
		else if (! status.getStatusCode().getValue().equals(StatusCode.SUCCESS_URI)) {
			String s = samlHelper.prettyPrint(status);
			logger.info("SAML Response Status: {}", s);
			throw new SamlAuthenticationException("SAML Response: Login was not successful " + status.getStatusCode().getValue());
		}
	}

	public void validateIdpSignature(SignableSAMLObject signableSamlObject, Issuer issuer, EntityDescriptor entityDescriptor) 
			throws SamlAuthenticationException {

		validateSignature(signableSamlObject, issuer, entityDescriptor, 
				IDPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS);
	}

	public void validateSpSignature(SignableSAMLObject signableSamlObject, Issuer issuer, EntityDescriptor entityDescriptor) 
			throws SamlAuthenticationException {
	
		validateSignature(signableSamlObject, issuer, entityDescriptor, 
				SPSSODescriptor.DEFAULT_ELEMENT_NAME, SAMLConstants.SAML20P_NS);
	}	
	
	protected void validateSignature(SignableSAMLObject signableSamlObject, Issuer issuer, EntityDescriptor entityDescriptor,
			QName role, String protocol) 
			throws SamlAuthenticationException {
	
		if (signableSamlObject.getSignature() == null)
			throw new SamlAuthenticationException("No Signature on SignableSamlObject");
		
		DOMMetadataProvider mp = new DOMMetadataProvider(entityDescriptor.getDOM());
		try {
			mp.initialize();
		} catch (MetadataProviderException e) {
			throw new SamlAuthenticationException("Metadata for IDP " + entityDescriptor.getEntityID() + " could not be established");			
		}
		
		MetadataCredentialResolver mdCredResolver = new MetadataCredentialResolver(mp);
		KeyInfoCredentialResolver keyInfoCredResolver =
			    Configuration.getGlobalSecurityConfiguration().getDefaultKeyInfoCredentialResolver();
		ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(mdCredResolver, keyInfoCredResolver);
		
		SAMLSignatureProfileValidator sigValidator = new SAMLSignatureProfileValidator();
		try {
			sigValidator.validate(signableSamlObject.getSignature());
		} catch (ValidationException e) {
			throw new SamlAuthenticationException("SAMLSignableObject signature is not valid");
		}
		
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIDCriteria(issuer.getValue()));
		criteriaSet.add(new MetadataCriteria(role, protocol));
		criteriaSet.add(new UsageCriteria(UsageType.SIGNING));
			
		try {
			if (trustEngine.validate(signableSamlObject.getSignature(), criteriaSet))
				logger.info("Signutare validation success for " + entityDescriptor.getEntityID());
			else {
				throw new SamlAuthenticationException("SAMLSignableObject could not be validated.");
			}
		} catch (SecurityException e) {
			throw new SamlAuthenticationException("SAMLSignableObject could not be validated.");
		}
	}	
}
