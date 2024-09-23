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

import java.time.Duration;
import java.time.Instant;

import javax.xml.namespace.QName;

import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.PredicateRoleDescriptorResolver;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.security.impl.MetadataCredentialResolver;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.criteria.UsageCriterion;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.saml.exc.SamlInvalidIssuerException;
import edu.kit.scc.webreg.service.saml.exc.SamlMissingIssuerException;
import edu.kit.scc.webreg.service.saml.exc.SamlMissingStatusException;
import edu.kit.scc.webreg.service.saml.exc.SamlResponseExpiredException;
import edu.kit.scc.webreg.service.saml.exc.SamlUnknownPrincipalException;
import edu.kit.scc.webreg.service.saml.exc.SamlUnsuccessfulStatusException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.resolver.CriteriaSet;

@ApplicationScoped
public class Saml2ResponseValidationService {

	@Inject
	private Logger logger;
	
	@Inject
	private SamlHelper samlHelper;

	public void verifyIssuer(SamlSpMetadataEntity metadataEntity,
			Response samlResponse) throws SamlAuthenticationException {
		verifyIssuer(metadataEntity, samlResponse.getIssuer());
	}

	public void verifyIssuer(SamlSpMetadataEntity metadataEntity,
			AttributeQuery attributeQuery) throws SamlAuthenticationException {
		verifyIssuer(metadataEntity, attributeQuery.getIssuer());
	}

	public void verifyIssuer(SamlSpMetadataEntity metadataEntity,
			Issuer issuer) throws SamlAuthenticationException {

		if (issuer == null)
			throw new SamlMissingIssuerException("Response issuer is not set");

		String issuerString = issuer.getValue();
		if (! issuerString.equals(metadataEntity.getEntityId())) 
			throw new SamlInvalidIssuerException("Response issuer " + issuerString + 
					" differs from excpected " + metadataEntity.getEntityId());

	}

	public void verifyIssuer(SamlMetadataEntity metadataEntity,
			Response samlResponse) throws SamlAuthenticationException {
		verifyIssuer(metadataEntity, samlResponse.getIssuer());
	}

	public void verifyIssuer(SamlMetadataEntity metadataEntity,
			Issuer issuer) throws SamlAuthenticationException {

		if (issuer == null)
			throw new SamlMissingIssuerException("Response issuer is not set");

		String issuerString = issuer.getValue();
		if (! issuerString.equals(metadataEntity.getEntityId())) 
			throw new SamlInvalidIssuerException("Response issuer " + issuerString + 
					" differs from excpected " + metadataEntity.getEntityId());

	}

	public void verifyExpiration(Response samlResponse, Long expiryMillis) 
			throws SamlAuthenticationException {

		Duration duration = Duration.between(samlResponse.getIssueInstant(), Instant.now());
		if (duration.compareTo(Duration.ofMillis(expiryMillis)) > 0) 
			throw new SamlResponseExpiredException("Response is already expired after " + duration.getSeconds() + " seconds");
	}	

	public void verifyStatus(Response samlResponse) 
			throws SamlAuthenticationException {

		if (samlResponse.getStatus() == null || samlResponse.getStatus().getStatusCode() == null)
			throw new SamlMissingStatusException("SAML Response does not contain a status code");
			
		Status status = samlResponse.getStatus();
		if (status.getStatusCode().getStatusCode() != null &&
				StatusCode.UNKNOWN_PRINCIPAL.equals(status.getStatusCode().getStatusCode().getValue())) {
			String s = samlHelper.prettyPrint(status);
			logger.info("SAML Response Status: {}", s);
			throw new SamlUnknownPrincipalException("SAML Response: Unknown Principal " + status.getStatusCode().getValue());
		}
		else if (! status.getStatusCode().getValue().equals(StatusCode.SUCCESS)) {
			String s = samlHelper.prettyPrint(status);
			logger.info("SAML Response Status: {}", s);
			throw new SamlUnsuccessfulStatusException("SAML Response: Login was not successful " + status.getStatusCode().getValue());
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
		
		DOMMetadataResolver mp = new DOMMetadataResolver(entityDescriptor.getDOM());
		mp.setId(entityDescriptor.getEntityID() + "-resolver");
		
		PredicateRoleDescriptorResolver roleResolver = new PredicateRoleDescriptorResolver(mp);
		// deprecated
		//BasicRoleDescriptorResolver roleResolver = new BasicRoleDescriptorResolver(mp);
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
			throw new SamlAuthenticationException("Cannot init MDCredResolver", e);
		}
		
		ExplicitKeySignatureTrustEngine trustEngine = new ExplicitKeySignatureTrustEngine(mdCredResolver, keyInfoCredResolver);
		
		SAMLSignatureProfileValidator sigValidator = new SAMLSignatureProfileValidator();
		try {
			sigValidator.validate(signableSamlObject.getSignature());
		} catch (SignatureException e) {
			throw new SamlAuthenticationException("SAMLSignableObject signature is not valid");
		}
		
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIdCriterion(issuer.getValue()));
		criteriaSet.add(new EntityRoleCriterion(role));
		criteriaSet.add(new UsageCriterion(UsageType.SIGNING));
			
		try {
			if (trustEngine.validate(signableSamlObject.getSignature(), criteriaSet))
				logger.info("Signutare validation success for " + entityDescriptor.getEntityID());
			else {
				throw new SamlAuthenticationException("SAMLSignableObject could not be validated.");
			}
		} catch (org.opensaml.security.SecurityException e) {
			if (e.getCause() != null)
				logger.info("Could not validate signature: {}, cause is: {}", e.getMessage(), e.getCause().getMessage());
			else
				logger.info("Could not validate signature: {}", e.getMessage());
			throw new SamlAuthenticationException("SAMLSignableObject could not be validated.");
		}
	}	
}
