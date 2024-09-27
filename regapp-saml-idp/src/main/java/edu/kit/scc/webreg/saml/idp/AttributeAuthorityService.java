package edu.kit.scc.webreg.saml.idp;

import java.time.Instant;
import java.util.HashSet;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthUserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.attribute.release.AttributeBuilder;
import edu.kit.scc.webreg.service.identity.IdentityAttributeResolver;
import edu.kit.scc.webreg.service.impl.OAuthUserUpdater;
import edu.kit.scc.webreg.service.impl.OidcUserUpdater;
import edu.kit.scc.webreg.service.impl.SamlUserUpdater;
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
	private ScriptDao scriptDao;

	@Inject
	private ScriptingEnv scriptingEnv;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private SsoHelper ssoHelper;

	@Inject
	private AttributeBuilder attributeBuilder;

	@Inject
	private IdentityAttributeResolver attributeResolver;

	@Inject
	private SamlAttributeTranscoder attributeTranscoder;

	@Inject
	private SamlUserUpdater samlUserUpdater;

	@Inject
	private OidcUserUpdater oidcUserUpdater;

	@Inject
	private OAuthUserUpdater oauthUserUpdater;

	public Envelope processAttributeQuery(SamlAAConfigurationEntity aaConfig, AttributeQuery query)
			throws SamlAuthenticationException {

		logger.debug("Processing AttributeQuery");

		Issuer issuer = query.getIssuer();
		if (issuer == null || issuer.getValue() == null) {
			throw new SamlAuthenticationException("Issuer not set");
		}

		String issuerString = issuer.getValue();
		SamlSpMetadataEntity spEntity = spMetadataDao.findByEntityId(issuerString);
		if (spEntity == null)
			throw new SamlAuthenticationException("Issuer metadata not in database");

		if (!spEntity.getGenericStore().containsKey("aq_allowed"))
			throw new SamlAuthenticationException("Issuer not allowed for attribute query");
		else if (!spEntity.getGenericStore().get("aq_allowed").equalsIgnoreCase("true")) {
			throw new SamlAuthenticationException("Issuer not allowed for attribute query");
		}

		EntityDescriptor spEntityDescriptor = samlHelper.unmarshal(spEntity.getEntityDescriptor(),
				EntityDescriptor.class);

		saml2ValidationService.verifyIssuer(spEntity, query);
		saml2ValidationService.validateSpSignature(query, issuer, spEntityDescriptor);

		Response samlResponse = buildSamlRespone(StatusCode.SUCCESS, null);
		samlResponse.setIssuer(ssoHelper.buildIssuser(aaConfig.getEntityId()));
		samlResponse.setIssueInstant(Instant.now());

		if (query.getSubject() != null && query.getSubject().getNameID() != null) {

			if (!spEntity.getGenericStore().containsKey("aq_resolve_user_scipt"))
				throw new SamlAuthenticationException(
						"NameId Resolver not configured, cannot resolve account. This is a server side error, contact the server administrator");
			String resolveUserScript = spEntity.getGenericStore().get("aq_resolve_user_scipt");

			if (!spEntity.getGenericStore().containsKey("aq_resolve_attribute_scipt"))
				throw new SamlAuthenticationException(
						"Attribute Resolver not configured, cannot resolve attributes. This is a server side error, contact the server administrator");
			String resolveAttributeScript = spEntity.getGenericStore().get("aq_resolve_attribute_scipt");

			ScriptEntity script = scriptDao.findByName(resolveUserScript);
			if (script == null)
				throw new SamlAuthenticationException(
						"NameId Resolver not configured correctly. Script not found: " + resolveUserScript);

			try {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(script.getScriptEngine());
				engine.eval(script.getScript());
				Invocable invocable = (Invocable) engine;

				String nameIdValue = query.getSubject().getNameID().getValue();
				String nameIdFormat = query.getSubject().getNameID().getFormat();

				final UserEntity user = resolveUser(invocable, nameIdFormat, nameIdValue, spEntity, aaConfig);
				final IdentityEntity identity = resolveIdentity(user, invocable, nameIdFormat, nameIdValue, spEntity,
						aaConfig);
				if (identity != null) {

					// only update all user objects when configured, because this can take some time
					if (spEntity.getGenericStore().containsKey("update_identity")
							&& spEntity.getGenericStore().get("update_identity").equalsIgnoreCase("true"))
						identity.getUsers().stream().forEach(u -> updateUser(u));

					AttributeReleaseEntity attributeRelease = attributeBuilder.requestAttributeRelease(spEntity,
							identity);

					script = scriptDao.findByName(resolveAttributeScript);
					if (script == null)
						throw new SamlAuthenticationException(
								"Attribute Resolver not configured correctly. Script not found: " + resolveUserScript);
					engine = (new ScriptEngineManager()).getEngineByName(script.getScriptEngine());
					engine.eval(script.getScript());
					invocable = (Invocable) engine;

					attributeRelease.setValuesToDelete(new HashSet<>(attributeRelease.getValues()));
					invocable.invokeFunction("resolveAttributes", scriptingEnv, attributeBuilder, attributeResolver,
							attributeRelease, identity, logger, spEntity, aaConfig);
					attributeRelease.getValuesToDelete().stream().forEach(v -> attributeBuilder.deleteValue(v));

					Assertion assertion = attributeTranscoder.convertAttributes(attributeRelease, aaConfig, spEntity,
							query);
					samlResponse.getAssertions().add(assertion);
				}
			} catch (NoSuchMethodException e) {
				logger.warn("Method is missing in script: {}", e.getMessage());
				throw new SamlAuthenticationException(
						"NameId Resolver not configured correctly. Method resolveUser not found in "
								+ resolveUserScript);
			} catch (ScriptException e) {
				logger.warn("Script contains errors: {}", e.getMessage());
				throw new SamlAuthenticationException("NameId Resolver " + resolveUserScript + " contains errors");
			}
		} else {
			throw new SamlAuthenticationException("Subject or Subject Name ID is missing in request");
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

	private IdentityEntity resolveIdentity(UserEntity user, Invocable invocable, String nameIdFormat,
			String nameIdValue, SamlSpMetadataEntity spEntity, SamlAAConfigurationEntity aaConfig)
			throws ScriptException {
		if (user != null) {
			return user.getIdentity();
		}

		try {
			return (IdentityEntity) invocable.invokeFunction("resolveIdentity", scriptingEnv, nameIdFormat, nameIdValue,
					logger, spEntity, aaConfig);
		} catch (NoSuchMethodException e) {
			// Ignore exception. Better would be a check for method existance
			return null;
		}

	}

	private UserEntity resolveUser(Invocable invocable, String nameIdFormat, String nameIdValue,
			SamlSpMetadataEntity spEntity, SamlAAConfigurationEntity aaConfig) throws ScriptException {
		try {
			return (UserEntity) invocable.invokeFunction("resolveUser", scriptingEnv, nameIdFormat, nameIdValue, logger,
					spEntity, aaConfig);
		} catch (NoSuchMethodException e) {
			// Ignore exception. Better would be a check for method existance
			return null;
		}

	}

	private void updateUser(UserEntity user) {
		try {
			if (user instanceof SamlUserEntity)
				samlUserUpdater.updateUserFromHomeOrg((SamlUserEntity) user, null, "attribue-query", null);
			else if (user instanceof OidcUserEntity)
				oidcUserUpdater.updateUserFromHomeOrg((OidcUserEntity) user, null, "attribue-query", null);
			else if (user instanceof OAuthUserEntity)
				oauthUserUpdater.updateUserFromHomeOrg((OAuthUserEntity) user, null, "attribue-query", null);
		} catch (UserUpdateException e) {
		}
	}
}
