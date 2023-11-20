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

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;

import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("ssoHelper")
@ApplicationScoped
public class SsoHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SamlHelper samlHelper;

	public AuthnRequest buildAuthnRequest(String spEntityId, String acs, String binding, Map<String, String> options,
			String destination) {

		AuthnRequest authnRequest = samlHelper.create(AuthnRequest.class, AuthnRequest.DEFAULT_ELEMENT_NAME);
		authnRequest.setID(samlHelper.getRandomId());
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setIssueInstant(Instant.now());
		if (options != null
				&& (options.containsKey("force_authn") && options.get("force_authn").equalsIgnoreCase("true"))) {
			authnRequest.setForceAuthn(true);
		} else {
			authnRequest.setForceAuthn(false);
		}
		authnRequest.setIsPassive(false);
		authnRequest.setProtocolBinding(binding);
		authnRequest.setAssertionConsumerServiceURL(acs);
		authnRequest.setDestination(destination);

		Issuer issuer = samlHelper.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(spEntityId);
		authnRequest.setIssuer(issuer);

		if (options != null
				&& (options.containsKey("nameid_policy") && options.get("nameid_policy").equalsIgnoreCase("none"))) {
			// No nameid policy
		} else {
			// standard case, when no nameid policy is set
			NameIDPolicy nameIdPolicy = samlHelper.create(NameIDPolicy.class, NameIDPolicy.DEFAULT_ELEMENT_NAME);
			nameIdPolicy.setAllowCreate(true);
			authnRequest.setNameIDPolicy(nameIdPolicy);
		}

		return authnRequest;
	}

	public Response buildAuthnResponse(AuthnRequest authnRequest, String spEntityId) {
		Response response = samlHelper.create(Response.class, Response.DEFAULT_ELEMENT_NAME);
		response.setID(samlHelper.getRandomId());
		response.setInResponseTo(authnRequest.getID());
		response.setVersion(SAMLVersion.VERSION_20);
		response.setIssueInstant(Instant.now());
		response.setDestination(authnRequest.getAssertionConsumerServiceURL());

		Issuer issuer = samlHelper.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(spEntityId);
		response.setIssuer(issuer);

		Status status = samlHelper.create(Status.class, Status.DEFAULT_ELEMENT_NAME);
		StatusCode statusCode = samlHelper.create(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
		statusCode.setValue(StatusCode.SUCCESS);
		status.setStatusCode(statusCode);
		response.setStatus(status);

		return response;
	}

	public Issuer buildIssuser(String entityId) {
		Issuer issuer = samlHelper.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(entityId);
		return issuer;
	}

	public Subject buildSubject(SamlIdpConfigurationEntity idpConfig, SamlSpMetadataEntity spMetadata,
			String nameIdValue, String nameIdType, String inResponseTo, String acs) {
		NameID nameId = samlHelper.create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameId.setFormat(nameIdType);
		nameId.setValue(nameIdValue);
		nameId.setNameQualifier(idpConfig.getEntityId());
		nameId.setSPNameQualifier(spMetadata.getEntityId());

		SubjectConfirmationData scd = samlHelper.create(SubjectConfirmationData.class,
				SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		scd.setNotOnOrAfter((new Date(System.currentTimeMillis() + (5L * 60L * 1000L))).toInstant());
		scd.setInResponseTo(inResponseTo);
		scd.setRecipient(acs);

		SubjectConfirmation sc = samlHelper.create(SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		sc.setMethod(SubjectConfirmation.METHOD_BEARER);
		sc.setSubjectConfirmationData(scd);

		Subject subject = samlHelper.create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
		subject.setNameID(nameId);
		subject.getSubjectConfirmations().add(sc);
		return subject;
	}

	public Subject buildAQSubject(SamlAAConfigurationEntity idpConfig, SamlSpMetadataEntity spMetadata,
			String nameIdValue, String nameIdType, String inResponseTo) {
		NameID nameId = samlHelper.create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameId.setFormat(nameIdType);
		nameId.setValue(nameIdValue);
		nameId.setNameQualifier(idpConfig.getEntityId());
		nameId.setSPNameQualifier(spMetadata.getEntityId());

		SubjectConfirmationData scd = samlHelper.create(SubjectConfirmationData.class,
				SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
		scd.setNotOnOrAfter((new Date(System.currentTimeMillis() + (5L * 60L * 1000L))).toInstant());
		scd.setInResponseTo(inResponseTo);

		SubjectConfirmation sc = samlHelper.create(SubjectConfirmation.class, SubjectConfirmation.DEFAULT_ELEMENT_NAME);
		sc.setMethod(SubjectConfirmation.METHOD_BEARER);
		sc.setSubjectConfirmationData(scd);

		Subject subject = samlHelper.create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
		subject.setNameID(nameId);
		subject.getSubjectConfirmations().add(sc);
		return subject;
	}

	public Conditions buildConditions(SamlSpMetadataEntity spMetadata) {
		Audience audience = samlHelper.create(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
		audience.setURI(spMetadata.getEntityId());
		AudienceRestriction ar = samlHelper.create(AudienceRestriction.class, AudienceRestriction.DEFAULT_ELEMENT_NAME);
		ar.getAudiences().add(audience);

		Conditions conditions = samlHelper.create(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
		conditions.setNotBefore(Instant.now());
		conditions.setNotOnOrAfter((new Date(System.currentTimeMillis() + (5L * 60L * 1000L))).toInstant());
		conditions.getAudienceRestrictions().add(ar);

		return conditions;
	}

	public AuthnStatement buildAuthnStatement(long validityInterval) {
		AuthnContextClassRef accr = samlHelper.create(AuthnContextClassRef.class,
				AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
		accr.setURI(AuthnContext.PPT_AUTHN_CTX);
		AuthnContext ac = samlHelper.create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
		ac.setAuthnContextClassRef(accr);
		AuthnStatement as = samlHelper.create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
		as.setAuthnContext(ac);
		as.setAuthnInstant(Instant.now());
		as.setSessionNotOnOrAfter((new Date(System.currentTimeMillis() + validityInterval)).toInstant());
		as.setSessionIndex(samlHelper.getRandomId());

		return as;
	}

	public Attribute buildSamlUriAttribute(String name, String friendlyName, String... values) {
		return buildAttribute(name, friendlyName, Attribute.URI_REFERENCE, values);
	}

	public Attribute buildAttribute(String name, String friendlyName, String nameFormat, String... values) {
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
