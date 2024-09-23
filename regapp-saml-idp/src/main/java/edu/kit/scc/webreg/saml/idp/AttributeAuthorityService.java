package edu.kit.scc.webreg.saml.idp;

import java.time.Instant;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.soap.soap11.Body;
import org.opensaml.soap.soap11.Envelope;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.SamlSpMetadataDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.saml.Saml2ResponseValidationService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SsoHelper;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AttributeAuthorityService {

	@Inject
	private Logger logger;

	@Inject
	private Saml2ResponseValidationService saml2ValidationService;

	@Inject
	private SamlSpMetadataDao spMetadataDao;

	@Inject
	private UserDao userService;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private SsoHelper ssoHelper;

	public Envelope processAttributeQuery(SamlAAConfigurationEntity aaConfig, AttributeQuery query) throws SamlAuthenticationException {
		
		logger.debug("Processing AttributeQuery");
		
		Issuer issuer = query.getIssuer();
		if (issuer == null || issuer.getValue() == null) {
			throw new SamlAuthenticationException("Issuer not set");
		}

		String issuerString = issuer.getValue();
		SamlSpMetadataEntity spEntity = spMetadataDao.findByEntityId(issuerString);
		if (spEntity == null)
			throw new SamlAuthenticationException("Issuer metadata not in database");

		EntityDescriptor spEntityDescriptor = samlHelper.unmarshal(spEntity.getEntityDescriptor(),
				EntityDescriptor.class);

		saml2ValidationService.verifyIssuer(spEntity, query);
		saml2ValidationService.validateSpSignature(query, issuer, spEntityDescriptor);

		Response samlResponse = buildSamlRespone(StatusCode.SUCCESS, null);
		samlResponse.setIssuer(ssoHelper.buildIssuser(aaConfig.getEntityId()));
		samlResponse.setIssueInstant(Instant.now());

		if (query.getSubject() != null && query.getSubject().getNameID() != null) {
			String nameIdValue = query.getSubject().getNameID().getValue();
			String nameIdFormat = query.getSubject().getNameID().getFormat();

			UserEntity user = userService.fetch(Long.parseLong(nameIdValue));
			if (user != null) {
				Assertion assertion = samlHelper.create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
				assertion.setIssueInstant(Instant.now());
				assertion.setIssuer(ssoHelper.buildIssuser(aaConfig.getEntityId()));
				assertion.setSubject(ssoHelper.buildAQSubject(aaConfig, spEntity, nameIdValue, NameID.UNSPECIFIED,
						query.getID()));
				assertion.getAttributeStatements().add(buildAttributeStatement(user));
				samlResponse.getAssertions().add(assertion);
			}
		}

		return buildSoapEnvelope(samlResponse);
	}
	
	public Envelope buildErrorResponse(String statusCodeString, String messageString) {
		Response samlResponse = buildSamlRespone(statusCodeString, messageString);
		return buildSoapEnvelope(samlResponse);
	}
	
	private Response buildSamlRespone(String statusCodeString, String messageString) {
		Response samlResponse = samlHelper.create(Response.class, Response.DEFAULT_ELEMENT_NAME);
		samlResponse.setStatus(buildSamlStatus(statusCodeString, messageString));
		return samlResponse;
	}

	private Status buildSamlStatus(String statusCodeString, String messageString) {
		StatusCode statusCode = samlHelper.create(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
		statusCode.setValue(statusCodeString);

		Status samlStatus = samlHelper.create(Status.class, Status.DEFAULT_ELEMENT_NAME);
		samlStatus.setStatusCode(statusCode);

		if (messageString != null) {
			StatusMessage statusMessage = samlHelper.create(StatusMessage.class, StatusMessage.DEFAULT_ELEMENT_NAME);
			statusMessage.setValue(messageString);
			samlStatus.setStatusMessage(statusMessage);
		}
		return samlStatus;
	}

	private Envelope buildSoapEnvelope(XMLObject xmlObject) {
		XMLObjectBuilderFactory bf = samlHelper.getBuilderFactory();
		Envelope envelope = (Envelope) bf.getBuilder(Envelope.DEFAULT_ELEMENT_NAME)
				.buildObject(Envelope.DEFAULT_ELEMENT_NAME);
		Body body = (Body) bf.getBuilder(Body.DEFAULT_ELEMENT_NAME).buildObject(Body.DEFAULT_ELEMENT_NAME);

		body.getUnknownXMLObjects().add(xmlObject);
		envelope.setBody(body);
		return envelope;
	}

	private AttributeStatement buildAttributeStatement(UserEntity user) {
		AttributeStatement attributeStatement = samlHelper.create(AttributeStatement.class,
				AttributeStatement.DEFAULT_ELEMENT_NAME);
		attributeStatement.getAttributes().add(buildAttribute("urn:oid:1.3.6.1.4.1.5923.1.1.1.6",
				"eduPersonPrincipalName", Attribute.URI_REFERENCE, user.getEppn()));
		return attributeStatement;
	}

	private Attribute buildAttribute(String name, String friendlyName, String nameFormat, String... values) {
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
