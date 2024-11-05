package edu.kit.scc.webreg.service.saml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.profile.context.EncryptionContext;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.SecurityException;
import org.opensaml.security.credential.BasicCredential;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.EncryptionParameters;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.EncryptionException;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.opensaml.xmlsec.keyinfo.impl.BasicKeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlAuthnRequestDao;
import edu.kit.scc.webreg.dao.SamlIdpConfigurationDao;
import edu.kit.scc.webreg.dao.SamlSpMetadataDao;
import edu.kit.scc.webreg.dao.ServiceSamlSpDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlAuthnRequestEntity;
import edu.kit.scc.webreg.entity.SamlIdpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceSamlSpEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.AttributeReleaseEntity;
import edu.kit.scc.webreg.entity.attribute.ReleaseStatusType;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.saml.idp.SamlAttributeTranscoder;
import edu.kit.scc.webreg.service.attribute.release.AttributeBuilder;
import edu.kit.scc.webreg.service.identity.IdentityAttributeResolver;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import net.shibboleth.shared.component.ComponentInitializationException;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import net.shibboleth.shared.servlet.impl.ThreadLocalHttpServletResponseSupplier;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SamlIdpServiceImpl implements SamlIdpService {

	@Inject
	private Logger logger;

	@Inject
	private UserDao userDao;

	@Inject
	private IdentityDao identityDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private SamlIdpConfigurationDao idpConfigDao;

	@Inject
	private SamlAuthnRequestDao samlAuthnRequestDao;

	@Inject
	private SamlSpMetadataDao spDao;

	@Inject
	private ServiceSamlSpDao serviceSamlSpDao;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private SsoHelper ssoHelper;

	@Inject
	private CryptoHelper cryptoHelper;

	@Inject
	private SamlScriptingEnv scriptingEnv;

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;

	@Inject
	private SessionManager session;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private AttributeBuilder attributeBuilder;

	@Inject
	private IdentityAttributeResolver attributeResolver;

	@Inject
	private SamlAttributeTranscoder attributeTranscoder;

	@Override
	@RetryTransaction
	public long registerAuthnRequest(AuthnRequest authnRequest) {
		SamlAuthnRequestEntity authnRequestEntity = samlAuthnRequestDao.createNew();
		authnRequestEntity.setValidUntil(new Date(System.currentTimeMillis() + 30L * 60L * 1000L));
		authnRequestEntity.setAuthnrequestData(samlHelper.prettyPrint(authnRequest));
		authnRequestEntity = samlAuthnRequestDao.persist(authnRequestEntity);
		return authnRequestEntity.getId();
	}

	@Override
	@RetryTransaction
	public String resumeAuthnRequest(Long authnRequestId, Long identityId, Long authnRequestIdpConfigId,
			String relayState, HttpServletResponse response) throws SamlAuthenticationException {

		SamlIdpConfigurationEntity idpConfig = idpConfigDao.fetch(authnRequestIdpConfigId);
		logger.debug("IDP Config loaded: {}", idpConfig.getEntityId());

		IdentityEntity identity = identityDao.fetch(identityId);
		logger.debug("Identity loaded: {}", identity.getId());

		// User pref user
		// TODO Change to something more correct. Script must choose user from identity
		// ie.
		List<UserEntity> userList = userDao.findByIdentity(identity);
		UserEntity user = identity.getPrefUser();
		if (user == null)
			user = userList.get(0);

		SamlAuthnRequestEntity authnRequestEntity = samlAuthnRequestDao.fetch(authnRequestId);
		AuthnRequest authnRequest = samlHelper.unmarshal(authnRequestEntity.getAuthnrequestData(), AuthnRequest.class);

		logger.debug("Authn request reloaded: {}", samlHelper.prettyPrint(authnRequest));

		SamlSpMetadataEntity spMetadata = spDao.findByEntityId(authnRequest.getIssuer().getValue());
		logger.debug("Corresponding SP found in Metadata: {}", spMetadata.getEntityId());
		authnRequestEntity.setSpMetadata(spMetadata);

		List<ServiceSamlSpEntity> serviceSamlSpEntityList = serviceSamlSpDao.findBySamlSp(spMetadata);

		if (serviceSamlSpEntityList.size() == 0) {
			/*
			 * there is no configured service for this SAML SP. This is not supported at the
			 * moment
			 */
			throw new SamlAuthenticationException("Nothiing configured for this SAML SP");
		}

		List<ServiceSamlSpEntity> filteredServiceSamlSpEntityList = new ArrayList<ServiceSamlSpEntity>();
		RegistryEntity registry = null;
		Boolean wantsElevation = false;
		long elevationTime = 5L * 60L * 1000L;
		if (appConfig.getConfigValue("elevation_time") != null) {
			elevationTime = Long.parseLong(appConfig.getConfigValue("elevation_time"));
		}

		for (ServiceSamlSpEntity serviceSamlSpEntity : serviceSamlSpEntityList) {
			ServiceEntity service = serviceSamlSpEntity.getService();

			if (service == null) {
				logger.debug("No Service for SP connected for {}", serviceSamlSpEntity.getId());
			} else {
				logger.debug("Service for SP found: {}, (serviceSamlSpEntity id {})", service.getId(),
						serviceSamlSpEntity.getId());
			}

			if (serviceSamlSpEntity.getIdp() != null
					&& (!serviceSamlSpEntity.getIdp().getId().equals(idpConfig.getId()))) {
				logger.debug("Specific IDP {} is set and not matching {}.", serviceSamlSpEntity.getIdp().getId(),
						idpConfig.getId());
			} else {
				/*
				 * If the service <-> saml sp connection has no specific idp set, or the idp
				 * matches the request evaluate all the scripts and create the user attributes
				 */
				if (matchService(serviceSamlSpEntity.getScript(), user, serviceSamlSpEntity)) {
					logger.debug("serviceSamlSpEntity matches: {}", serviceSamlSpEntity.getId());

					if (service != null) {
						registry = registryDao.findByServiceAndIdentityAndStatus(service, identity,
								RegistryStatus.ACTIVE);
						if (registry != null) {
							List<Object> objectList = checkRules(user, service, registry);
							List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
							List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

							if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
								return "/user/check-access.xhtml?regId=" + registry.getId();
							}

							filteredServiceSamlSpEntityList.add(serviceSamlSpEntity);
						} else {
							registry = registryDao.findByServiceAndIdentityAndStatus(service, identity,
									RegistryStatus.LOST_ACCESS);

							if (registry != null) {
								logger.info(
										"Registration for user {} and service {} in state LOST_ACCESS, checking again",
										user.getEppn(), service.getName());
								List<Object> objectList = checkRules(user, service, registry);
								List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
								List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

								if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
									logger.info(
											"Registration for user {} and service {} in state LOST_ACCESS stays, redirecting to check page",
											user.getEppn(), service.getName());
									return "/user/check-access.xhtml?regId=" + registry.getId();
								}

								filteredServiceSamlSpEntityList.add(serviceSamlSpEntity);
							} else {
								logger.info(
										"No active registration for user {} and service {}, redirecting to register page",
										user.getEppn(), service.getName());
								return "/user/register-service.xhtml?serviceId=" + service.getId();
							}
						}
					} else {
						/*
						 * There is no service set for this sp idp connection
						 */
						filteredServiceSamlSpEntityList.add(serviceSamlSpEntity);
						List<String> unauthorizedList = knowledgeSessionService
								.checkScriptAccess(serviceSamlSpEntity.getScript(), identity);
						if (unauthorizedList.size() > 0) {
							return "/user/saml-access-denied.xhtml?soidc=" + serviceSamlSpEntity.getId();
						}
					}
				} else {
					logger.debug("serviceSamlSpEntity no match: {}", serviceSamlSpEntity.getId());
				}
			}

		}

		if (registry != null) {
			// Redefine user to match registry
			user = registry.getUser();
		}

		if (wantsElevation || evalTwoFa(user, registry, filteredServiceSamlSpEntityList, authnRequest)) {
			if (!isElevated(session, elevationTime)) {
				// second factor is active for this service and web login
				// and user is not elevated yet
				session.setOriginalRequestPath("/saml/idp/redirect/response");
				return "/user/twofa-login.xhtml";
			}
		}

		Response samlResponse = ssoHelper.buildAuthnResponse(authnRequest, idpConfig.getEntityId());

		Assertion assertion = samlHelper.create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setID(samlHelper.getRandomId());
		assertion.setIssueInstant(Instant.now());
		assertion.setIssuer(ssoHelper.buildIssuser(idpConfig.getEntityId()));
		assertion.setConditions(ssoHelper.buildConditions(spMetadata));

		String returnUrl = buildAttributeStatement(idpConfig, spMetadata, authnRequest, assertion, user,
				filteredServiceSamlSpEntityList, registry, authnRequestEntity);
		if (returnUrl != null)
			return returnUrl;

		long validity = 30L * 60L * 1000L;
		if (spMetadata.getGenericStore().containsKey("session_validity")) {
			validity = Long.parseLong(spMetadata.getGenericStore().get("session_validity"));
		}
		assertion.getAuthnStatements().add(ssoHelper.buildAuthnStatement(validity, isElevated(session, elevationTime)));

		SecurityParametersContext securityContext = buildSecurityContext(idpConfig);
		HTTPPostEncoder postEncoder = new HTTPPostEncoder();
		postEncoder.setHttpServletResponseSupplier(new ThreadLocalHttpServletResponseSupplier());
		MessageContext messageContext = new MessageContext();

		logger.debug("Assertion before encryption: {}", samlHelper.prettyPrint(assertion));

		/*
		 * encrypt assertion
		 */
		try {
			samlResponse.getEncryptedAssertions().add(encryptAssertion(assertion, spMetadata, messageContext));
		} catch (SamlAuthenticationException e) {
			throw new SamlAuthenticationException(e);
		}

		messageContext.setMessage(samlResponse);

		/*
		 * signing response
		 */
		messageContext.addSubcontext(securityContext);

		SAMLPeerEntityContext entityContext = new SAMLPeerEntityContext();
		SAMLEndpointContext endpointContext = new SAMLEndpointContext();

		AssertionConsumerService acs = samlHelper.create(AssertionConsumerService.class,
				AssertionConsumerService.DEFAULT_ELEMENT_NAME);
		acs.setLocation(authnRequest.getAssertionConsumerServiceURL());
		endpointContext.setEndpoint(acs);
		entityContext.addSubcontext(endpointContext);
		messageContext.addSubcontext(entityContext);

		try {
			SAMLMessageSecuritySupport.signMessage(messageContext);
		} catch (SecurityException | MarshallingException | SignatureException e) {
			throw new SamlAuthenticationException(e);
		}

		if (relayState != null) {
			messageContext.ensureSubcontext(SAMLBindingContext.class).setRelayState(relayState);
		}

		postEncoder.setMessageContext(messageContext);

		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.setProperty("resource.loader", "class");
		engine.setProperty("class.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		engine.init();
		postEncoder.setVelocityEngine(engine);

		logger.debug(samlHelper.prettyPrint(samlResponse));

		try {
			postEncoder.initialize();
			postEncoder.encode();

			return null;
		} catch (MessageEncodingException | ComponentInitializationException e) {
			logger.warn("Exception occured", e);
			throw new SamlAuthenticationException(e);
		}
	}

	private Boolean isElevated(SessionManager session, long elevationTime) {
		return (session.getTwoFaElevation() != null
				&& (System.currentTimeMillis() - session.getTwoFaElevation().toEpochMilli()) < elevationTime);
	}

	private SecurityParametersContext buildSecurityContext(SamlIdpConfigurationEntity idpConfig)
			throws SamlAuthenticationException {
		PrivateKey privateKey;
		X509Certificate publicKey;
		try {
			privateKey = cryptoHelper.getPrivateKey(idpConfig.getPrivateKey());
			publicKey = cryptoHelper.getCertificate(idpConfig.getCertificate());
		} catch (IOException e) {
			throw new SamlAuthenticationException("Private key is not set up properly", e);
		}

		BasicX509Credential credential = new BasicX509Credential(publicKey, privateKey);
		List<Credential> credentialList = new ArrayList<Credential>();
		credentialList.add(credential);

		BasicSignatureSigningConfiguration ssConfig = DefaultSecurityConfigurationBootstrap
				.buildDefaultSignatureSigningConfiguration();
		ssConfig.setSigningCredentials(credentialList);
		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new SignatureSigningConfigurationCriterion(ssConfig));
		SAMLMetadataSignatureSigningParametersResolver smsspr = new SAMLMetadataSignatureSigningParametersResolver();

		SignatureSigningParameters ssp;
		try {
			ssp = smsspr.resolveSingle(criteriaSet);
		} catch (ResolverException e) {
			throw new SamlAuthenticationException(e);
		}
		logger.debug("Resolved algo {} for signing", ssp.getSignatureAlgorithm());
		SecurityParametersContext securityContext = new SecurityParametersContext();
		securityContext.setSignatureSigningParameters(ssp);

		return securityContext;
	}

	private EncryptedAssertion encryptAssertion(Assertion assertion, SamlSpMetadataEntity spMetadata,
			MessageContext messageContext) throws SamlAuthenticationException {

		EntityDescriptor ed = samlHelper.unmarshal(spMetadata.getEntityDescriptor(), EntityDescriptor.class);

		KeyDescriptor keyDescriptor = null;
		SPSSODescriptor spsso = ed.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
		for (KeyDescriptor kd : spsso.getKeyDescriptors()) {
			if (kd.getUse() == null || kd.getUse().equals(UsageType.ENCRYPTION)
					|| kd.getUse().equals(UsageType.UNSPECIFIED)) {
				keyDescriptor = kd;
				break;
			}
		}

		if (keyDescriptor == null) {
			return null;
		}

		KeyInfo keyInfo = keyDescriptor.getKeyInfo();
		X509Data x509Data = keyInfo.getX509Datas().get(0);
		org.opensaml.xmlsec.signature.X509Certificate x509cert = x509Data.getX509Certificates().get(0);
		String cert = x509cert.getValue();

		final String dataEncAlgo = spMetadata.getGenericStore().getOrDefault("enc_algo",
				EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
		final String keyTransAlgo = spMetadata.getGenericStore().getOrDefault("key_transport_algo",
				EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
		Encrypter enc = buildEncrypter(cert, messageContext, spMetadata.getEntityId(), dataEncAlgo, keyTransAlgo);
		try {
			return enc.encrypt(assertion);
		} catch (EncryptionException e) {
			throw new SamlAuthenticationException("exception", e);
		}
	}

	private Encrypter buildEncrypter(String cert, MessageContext messageContext, String spEntityId, String dataEncAlgo,
			String keyTransAlgo) throws SamlAuthenticationException {
		try {
			byte[] decodedCert = Base64.decodeBase64(cert);
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(decodedCert);
			X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(in);

			BasicCredential encryptCredential = new BasicCredential(certificate.getPublicKey());

			final BasicKeyInfoGeneratorFactory generator = new BasicKeyInfoGeneratorFactory();
			generator.setEmitPublicKeyValue(true);

			EncryptionParameters encParams = new EncryptionParameters();
			encParams.setDataEncryptionAlgorithm(dataEncAlgo);
			encParams.setDataKeyInfoGenerator(generator.newInstance());
			encParams.setKeyTransportEncryptionAlgorithm(keyTransAlgo);
			encParams.setKeyTransportEncryptionCredential(encryptCredential);
			encParams.setKeyTransportKeyInfoGenerator(generator.newInstance());

			messageContext.ensureSubcontext(EncryptionContext.class).setAssertionEncryptionParameters(encParams);

			Encrypter encrypter = new Encrypter(new DataEncryptionParameters(encParams),
					new KeyEncryptionParameters(encParams, spEntityId));
			return encrypter;
		} catch (CertificateException e) {
			throw new SamlAuthenticationException("Certificate cannot be read", e);
		}
	}

	private Boolean matchService(ScriptEntity scriptEntity, UserEntity user, ServiceSamlSpEntity serviceSamlSp)
			throws SamlAuthenticationException {
		ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

		if (engine == null)
			throw new SamlAuthenticationException(
					"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

		try {
			engine.eval(scriptEntity.getScript());

			Invocable invocable = (Invocable) engine;

			Object object = invocable.invokeFunction("matchService", scriptingEnv, user, serviceSamlSp.getService(),
					logger);

			if (object instanceof Boolean)
				return (Boolean) object;
			else
				return false;
		} catch (ScriptException e) {
			logger.warn("Script execution failed. Continue with other scripts.", e);
			return false;
		} catch (NoSuchMethodException e) {
			logger.debug("No matchService method in script. Assuming match true");
			return true;
		}
	}

	private Boolean evalTwoFa(UserEntity user, RegistryEntity registry, List<ServiceSamlSpEntity> serviceSamlSpList,
			AuthnRequest authnRequest) throws SamlAuthenticationException {
		if (authnRequest.getRequestedAuthnContext() != null
				&& authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs() != null) {
			for (AuthnContextClassRef accr : authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs()) {
				if (accr.getURI().equals("https://refeds.org/profile/mfa"))
					return true;
			}
		}

		for (ServiceSamlSpEntity serviceSamlSp : serviceSamlSpList) {
			if (serviceSamlSp.getWantsElevation() != null && serviceSamlSp.getWantsElevation()) {
				return true;
			}

			ScriptEntity scriptEntity = serviceSamlSp.getScript();
			ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

			if (engine == null)
				throw new SamlAuthenticationException(
						"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

			try {
				engine.eval(scriptEntity.getScript());

				Invocable invocable = (Invocable) engine;

				Object object = invocable.invokeFunction("evalTwoFa", scriptingEnv, user, registry, serviceSamlSp,
						authnRequest, logger);

				if (object instanceof Boolean && ((Boolean) object))
					return true;
			} catch (ScriptException e) {
				logger.warn("Script execution failed. Continue with other scripts.", e);
			} catch (NoSuchMethodException e) {
				logger.debug("No evalTwoFa method in script. Assuming match false");
			}
		}

		return false;
	}

	private String buildAttributeStatement(final SamlIdpConfigurationEntity idpConfig,
			final SamlSpMetadataEntity spMetadata, final AuthnRequest authnRequest, final Assertion assertion,
			final UserEntity user, final List<ServiceSamlSpEntity> serviceSamlSpEntityList,
			final RegistryEntity registry, final SamlAuthnRequestEntity authnRequestEntity)
			throws SamlAuthenticationException {

		List<Attribute> attributeList = new ArrayList<>();
		Boolean subjectOverride = false;

		final IdentityEntity identity = user.getIdentity();
		final AttributeReleaseEntity attributeRelease = attributeBuilder.requestAttributeRelease(spMetadata, identity);
		authnRequestEntity.setAttributeRelease(attributeRelease);
		attributeRelease.setValuesToDelete(new HashSet<>(attributeRelease.getValues()));

		for (ServiceSamlSpEntity serviceSamlSp : serviceSamlSpEntityList) {
			ScriptEntity scriptEntity = serviceSamlSp.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new SamlAuthenticationException(
							"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;

					try {
						invocable.invokeFunction("buildAttributeStatement", scriptingEnv, user, registry,
								serviceSamlSp.getService(), attributeList, logger);
					} catch (NoSuchMethodException e) {
						// Ignore, if this method is missing
					}

					try {
						invocable.invokeFunction("resolveAttributes", scriptingEnv, attributeBuilder, attributeResolver,
								attributeRelease, identity, user, registry, logger, spMetadata, idpConfig);
					} catch (NoSuchMethodException e) {
						// Ignore, if this method is missing
					}

					try {
						Object o = invocable.invokeFunction("buildNameId", scriptingEnv, user, registry,
								serviceSamlSp.getService(), idpConfig, spMetadata, authnRequest.getID(),
								authnRequest.getAssertionConsumerServiceURL(), logger);
						if (o instanceof Subject) {
							assertion.setSubject((Subject) o);
							subjectOverride = true;
						}
					} catch (NoSuchMethodException e) {
						// Ignore, if this method is missing
					}
				} catch (ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			} else {
				throw new SamlAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}

		}
		attributeRelease.getValuesToDelete().stream().forEach(v -> attributeBuilder.deleteValue(v));

		final AttributeStatement attributeStatement = attributeTranscoder.convertAttributeStatement(attributeRelease,
				idpConfig, spMetadata);

		for (Attribute attribute : attributeList) {
			attributeStatement.getAttributes().add(attribute);
		}

		if (!subjectOverride) {
			assertion.setSubject(ssoHelper.buildSubject(idpConfig, spMetadata, samlHelper.getRandomId(),
					NameID.TRANSIENT, authnRequest.getID(), authnRequest.getAssertionConsumerServiceURL()));
		}

		if (spMetadata.getGenericStore().containsKey("show_consent")
				&& spMetadata.getGenericStore().get("show_consent").equalsIgnoreCase("true")) {
			if (!ReleaseStatusType.GOOD.equals(attributeRelease.getReleaseStatus())) {
				// send client to attribute release page
				logger.debug("Attribute Release is not good, sending user to constent page");
				return "/user/attribute-release-saml.xhtml?id=" + attributeRelease.getId();
			}
		} else {
			attributeRelease.setReleaseStatus(null);
		}

		assertion.getAttributeStatements().add(attributeStatement);
		return null;
	}

	private List<Object> checkRules(UserEntity user, ServiceEntity service, RegistryEntity registry) {
		return knowledgeSessionService.checkServiceAccessRule(user, service, registry, "user-self", false);
	}

	private List<OverrideAccess> extractOverideAccess(List<Object> objectList) {
		List<OverrideAccess> returnList = new ArrayList<OverrideAccess>();

		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				returnList.add((OverrideAccess) o);
			}
		}

		return returnList;
	}

	private List<UnauthorizedUser> extractUnauthorizedUser(List<Object> objectList) {
		List<UnauthorizedUser> returnList = new ArrayList<UnauthorizedUser>();

		for (Object o : objectList) {
			if (o instanceof UnauthorizedUser) {
				returnList.add((UnauthorizedUser) o);
			}
		}

		return returnList;
	}
}
