package edu.kit.scc.webreg.saml.idp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPPostEncoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
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
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SamlScriptingEnv;
import edu.kit.scc.webreg.service.saml.SsoHelper;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

@Stateless
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
	
	@Override
	public long registerAuthnRequest(AuthnRequest authnRequest) {
		SamlAuthnRequestEntity authnRequestEntity = samlAuthnRequestDao.createNew();
		authnRequestEntity.setValidUntil(new Date(System.currentTimeMillis() + 30L * 60L * 1000L));
		authnRequestEntity.setAuthnrequestData(samlHelper.prettyPrint(authnRequest));
		authnRequestEntity = samlAuthnRequestDao.persist(authnRequestEntity);
		return authnRequestEntity.getId();
	}

	@Override
	public String resumeAuthnRequest(Long authnRequestId, Long identityId, Long authnRequestIdpConfigId,
			String relayState, HttpServletResponse response) throws SamlAuthenticationException {
		
		SamlIdpConfigurationEntity idpConfig = idpConfigDao.fetch(authnRequestIdpConfigId);
		logger.debug("IDP Config loaded: {}", idpConfig.getEntityId());
		
		IdentityEntity identity = identityDao.fetch(identityId);
		logger.debug("Identity loaded: {}", identity.getId());

		// First user object picked for now
		// TODO Change to something more correct. Script must choose user from identity ie.
		List<UserEntity> userList = userDao.findByIdentity(identity);
		UserEntity user = userList.get(0);
		
		SamlAuthnRequestEntity authnRequestEntity = samlAuthnRequestDao.fetch(authnRequestId);
		AuthnRequest authnRequest = samlHelper.unmarshal(authnRequestEntity.getAuthnrequestData(), AuthnRequest.class);
		
		logger.debug("Authn request reloaded: {}", samlHelper.prettyPrint(authnRequest));

		SamlSpMetadataEntity spMetadata = spDao.findByEntityId(authnRequest.getIssuer().getValue());
		logger.debug("Corresponding SP found in Metadata: {}", spMetadata.getEntityId());

		List<ServiceSamlSpEntity> serviceSamlSpEntityList = serviceSamlSpDao.findBySamlSp(spMetadata);
		
		if (serviceSamlSpEntityList.size() == 0) {
			/*
			 * there is no configured service for this SAML SP. This is not supported at the moment
			 */
			throw new SamlAuthenticationException("Nothiing configured for this SAML SP");
		}
		
		List<ServiceSamlSpEntity> filteredServiceSamlSpEntityList = new ArrayList<ServiceSamlSpEntity>();
		RegistryEntity registry = null;
		for (ServiceSamlSpEntity serviceSamlSpEntity : serviceSamlSpEntityList) {
			ServiceEntity service = serviceSamlSpEntity.getService();
			
			if (service == null) {
				logger.debug("No Service for SP connected for {}", serviceSamlSpEntity.getId());
			}
			else {
				logger.debug("Service for SP found: {}, (serviceSamlSpEntity id {})", service.getId(), serviceSamlSpEntity.getId());
			}

			if (serviceSamlSpEntity.getIdp() != null && 
					(! serviceSamlSpEntity.getIdp().getId().equals(idpConfig.getId()))) {
				logger.debug("Specific IDP {} is set and not matching {}.", serviceSamlSpEntity.getIdp().getId(), idpConfig.getId());
			}
			else {
				/*
				 * If the service <-> saml sp connection has no specific idp set, or the idp matches the request
				 * evaluate all the scripts and create the user attributes
				 */
				if (matchService(serviceSamlSpEntity.getScript(), user, serviceSamlSpEntity)) {
					logger.debug("serviceSamlSpEntity matches: {}", serviceSamlSpEntity.getId());
					
					if (service != null) {
						registry = registryDao.findByServiceAndIdentityAndStatus(service, identity, RegistryStatus.ACTIVE);
						if (registry != null) {
							List<Object> objectList = checkRules(user, service, registry);
							List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
							List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);
							
							if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
								return "/user/check-access.xhtml?regId=" + registry.getId();
							}

							filteredServiceSamlSpEntityList.add(serviceSamlSpEntity);
						}
						else {
							registry = registryDao.findByServiceAndIdentityAndStatus(service, identity, RegistryStatus.LOST_ACCESS);
							
							if (registry != null) {
								logger.info("Registration for user {} and service {} in state LOST_ACCESS, checking again", 
										user.getEppn(), service.getName());
								List<Object> objectList = checkRules(user, service, registry);
								List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
								List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);
								
								if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
									logger.info("Registration for user {} and service {} in state LOST_ACCESS stays, redirecting to check page", 
											user.getEppn(), service.getName());
									return "/user/check-access.xhtml?regId=" + registry.getId();
								}

								filteredServiceSamlSpEntityList.add(serviceSamlSpEntity);
							}
							else {
								logger.info("No active registration for user {} and service {}, redirecting to register page", 
										user.getEppn(), service.getName());
								return "/user/register-service.xhtml?serviceId=" + service.getId();
							}
						}
					}
					else {
						/*
						 * There is no service set for this sp idp connection
						 */
						filteredServiceSamlSpEntityList.add(serviceSamlSpEntity);
					}
				}
				else {
					logger.debug("serviceSamlSpEntity no match: {}", serviceSamlSpEntity.getId());				
				}				
			}
			
		}
		
		if (registry != null) {
			// Redefine user to match registry
			user = registry.getUser();
		}
		
		Response samlResponse = ssoHelper.buildAuthnResponse(authnRequest, idpConfig.getEntityId());
		
		Assertion assertion = samlHelper.create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
		assertion.setID(samlHelper.getRandomId());
		assertion.setIssueInstant(new DateTime());
		assertion.setIssuer(ssoHelper.buildIssuser(idpConfig.getEntityId()));
		assertion.setSubject(ssoHelper.buildSubject(idpConfig, spMetadata, samlHelper.getRandomId(), NameID.TRANSIENT, authnRequest.getID(), authnRequest.getAssertionConsumerServiceURL()));
		assertion.setConditions(ssoHelper.buildConditions(spMetadata));
		assertion.getAttributeStatements().add(buildAttributeStatement(user, filteredServiceSamlSpEntityList, registry));
		assertion.getAuthnStatements().add(ssoHelper.buildAuthnStatement((30L * 60L * 1000L)));
		
		SecurityParametersContext securityContext = buildSecurityContext(idpConfig);
		HTTPPostEncoder postEncoder = new HTTPPostEncoder();
		postEncoder.setHttpServletResponse(response);
		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();

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
		
		AssertionConsumerService acs = samlHelper.create(AssertionConsumerService.class, AssertionConsumerService.DEFAULT_ELEMENT_NAME);
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
			messageContext.getSubcontext(SAMLBindingContext.class, true).setRelayState(relayState);
		}
		
		postEncoder.setMessageContext(messageContext);

		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("runtime.log.logsystem.log4j.logger", "root");
		engine.setProperty("resource.loader", "class");
		engine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
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
	
	private SecurityParametersContext buildSecurityContext(SamlIdpConfigurationEntity idpConfig) throws SamlAuthenticationException {
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
		
		BasicSignatureSigningConfiguration ssConfig = DefaultSecurityConfigurationBootstrap.buildDefaultSignatureSigningConfiguration();
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
	
	private EncryptedAssertion encryptAssertion(Assertion assertion, SamlSpMetadataEntity spMetadata, MessageContext<?> messageContext) throws SamlAuthenticationException {

		EntityDescriptor ed = samlHelper.unmarshal(spMetadata.getEntityDescriptor(), EntityDescriptor.class);
		
		KeyDescriptor keyDescriptor = null;
		SPSSODescriptor spsso = ed.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
		for (KeyDescriptor kd : spsso.getKeyDescriptors()) {
			if (kd.getUse() == null || kd.getUse().equals(UsageType.ENCRYPTION)) {
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
		Encrypter enc = buildEncrypter(cert, messageContext, spMetadata.getEntityId());
		try {
			return enc.encrypt(assertion);
		} catch (EncryptionException e) {
			throw new SamlAuthenticationException("exception", e);
		}
	}
	
	private Encrypter buildEncrypter(String cert, MessageContext<?> messageContext, String spEntityId) 
			throws SamlAuthenticationException {
		try {
			byte[] decodedCert = Base64.decodeBase64(cert);
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		    InputStream in = new ByteArrayInputStream(decodedCert);
		    X509Certificate certificate = (X509Certificate)certFactory.generateCertificate(in);

		    BasicCredential encryptCredential = new BasicCredential(certificate.getPublicKey());

		    final BasicKeyInfoGeneratorFactory generator = new BasicKeyInfoGeneratorFactory();
			generator.setEmitPublicKeyValue(true);

		    EncryptionParameters encParams = new EncryptionParameters();
			encParams.setDataEncryptionAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
			encParams.setDataKeyInfoGenerator(generator.newInstance());
			encParams.setKeyTransportEncryptionAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
			encParams.setKeyTransportEncryptionCredential(encryptCredential);
			encParams.setKeyTransportKeyInfoGenerator(generator.newInstance());

			messageContext.getSubcontext(EncryptionContext.class, true)
				.setAssertionEncryptionParameters(encParams);

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
			throw new SamlAuthenticationException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

		try {
			engine.eval(scriptEntity.getScript());

			Invocable invocable = (Invocable) engine;
			
			Object object = invocable.invokeFunction("matchService", scriptingEnv, user, 
					serviceSamlSp.getService(), logger);
			
			if (object instanceof Boolean)
				return (Boolean) object;
			else
				return false;
		} catch (ScriptException e) {
			logger.warn("Script execution failed. Continue with other scripts.", e);
			return false;
		} catch (NoSuchMethodException e) {
			logger.info("No matchService method in script. Assuming match true");
			return true;
		}
	}
	
	private AttributeStatement buildAttributeStatement(UserEntity user, 
			List<ServiceSamlSpEntity> serviceSamlSpEntityList, RegistryEntity registry) 
				throws SamlAuthenticationException {
		
		List<Attribute> attributeList = new ArrayList<>();
		
		for (ServiceSamlSpEntity serviceSamlSp : serviceSamlSpEntityList) {
			ScriptEntity scriptEntity = serviceSamlSp.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new SamlAuthenticationException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());
				
				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;
					
					invocable.invokeFunction("buildAttributeStatement", scriptingEnv, user, registry, 
							serviceSamlSp.getService(), attributeList, logger);
				} catch (NoSuchMethodException | ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			}
			else {
				throw new SamlAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
			
		}
		AttributeStatement attributeStatement = samlHelper.create(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);
		for (Attribute attribute : attributeList) {
			attributeStatement.getAttributes().add(attribute);
		}
				
		return attributeStatement;
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
