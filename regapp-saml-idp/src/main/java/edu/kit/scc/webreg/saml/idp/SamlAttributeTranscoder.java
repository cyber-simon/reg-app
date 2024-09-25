package edu.kit.scc.webreg.saml.idp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.NameID;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.value.StringValueEntity;
import edu.kit.scc.webreg.entity.attribute.value.ValueEntity;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SsoHelper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SamlAttributeTranscoder {

	@Inject
	private Logger logger;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private SsoHelper ssoHelper;

	private Map<String, ValueTranscoder> transcoderMap;

	@PostConstruct
	public void init() {
		transcoderMap = new HashMap<>();
		transcoderMap.put("family_name",
				new SingleStringValueTranscoder(samlHelper, "urn:oid:2.5.4.4", "sn", Attribute.BASIC));
		transcoderMap.put("given_name",
				new SingleStringValueTranscoder(samlHelper, "urn:oid:2.5.4.42", "givenName", Attribute.BASIC));
		transcoderMap.put("eduperson_principal_name", new SingleStringValueTranscoder(samlHelper,
				"urn:oid:1.3.6.1.4.1.5923.1.1.1.6", "eduPersonPrincipalName", Attribute.URI_REFERENCE));
		transcoderMap.put("eduperson_entitlement", new SingleStringValueTranscoder(samlHelper,
				"urn:oid:1.3.6.1.4.1.5923.1.1.1.7", "eduPersonEntitlement", Attribute.BASIC));
	}

	public Assertion convertAttributes(AttributeReleaseEntity attributeRelease, SamlAAConfigurationEntity aaConfig,
			SamlSpMetadataEntity spEntity, AttributeQuery query) {
		Assertion assertion = samlHelper.create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setIssueInstant(Instant.now());
		assertion.setIssuer(ssoHelper.buildIssuser(aaConfig.getEntityId()));

		AttributeStatement attributeStatement = buildAttributeStatement();
		assertion.getAttributeStatements().add(attributeStatement);

		for (ValueEntity value : attributeRelease.getValues()) {
			if (value.getAttribute().getName().equals("sub")) {
				assertion.setSubject(ssoHelper.buildAQSubject(aaConfig, spEntity,
						((StringValueEntity) value).getValueString(), NameID.UNSPECIFIED, query.getID()));
			} else if (transcoderMap.containsKey(value.getAttribute().getName())) {
				attributeStatement.getAttributes()
						.add(transcoderMap.get(value.getAttribute().getName()).transcode(value));
			} else {
				logger.debug("No SAML Transcoder for attribute {}", value.getAttribute().getName());
			}
		}

		return assertion;
	}

	private AttributeStatement buildAttributeStatement() {
		AttributeStatement attributeStatement = samlHelper.create(AttributeStatement.class,
				AttributeStatement.DEFAULT_ELEMENT_NAME);
		return attributeStatement;
	}
}
