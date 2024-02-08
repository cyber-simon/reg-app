package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.opensaml.messaging.context.InOutOperationContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.pipeline.httpclient.BasicHttpClientMessagePipeline;
import org.opensaml.messaging.pipeline.httpclient.HttpClientMessagePipeline;
import org.opensaml.messaging.pipeline.httpclient.HttpClientMessagePipelineFactory;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.decoding.impl.HttpClientResponseSOAP11Decoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HttpClientRequestSOAP11Encoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.soap.client.http.PipelineFactoryHttpSOAPClient;
import org.opensaml.soap.common.SOAPException;
import org.opensaml.soap.messaging.context.SOAP11Context;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.slf4j.Logger;
import org.slf4j.MDC;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.drools.exc.UnauthorizedException;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.GenericRestInterfaceException;
import edu.kit.scc.webreg.exc.LoginFailedException;
import edu.kit.scc.webreg.exc.NoDelegationConfiguredException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoScopedUsernameException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UserNotRegisteredException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.exc.UserUpdateFailedException;
import edu.kit.scc.webreg.service.UserLoginService;
import edu.kit.scc.webreg.service.reg.PasswordUtil;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.Saml2ResponseValidationService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SamlIdentifier;
import edu.kit.scc.webreg.service.saml.SsoHelper;
import edu.kit.scc.webreg.service.saml.exc.AssertionException;
import edu.kit.scc.webreg.service.saml.exc.MetadataException;
import edu.kit.scc.webreg.service.saml.exc.NoEcpSupportException;
import edu.kit.scc.webreg.service.saml.exc.NoHostnameConfiguredException;
import edu.kit.scc.webreg.service.saml.exc.NoIdpForScopeException;
import edu.kit.scc.webreg.service.saml.exc.NoIdpFoundException;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.user.UserUpdater;
import edu.kit.scc.webreg.session.HttpRequestContext;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import net.shibboleth.shared.component.ComponentInitializationException;

@Stateless
public class UserLoginServiceImpl implements UserLoginService, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private PasswordUtil passwordUtil;

	@Inject
	private SamlUserDao samlUserDao;

	@Inject
	private UserUpdater userUpdater;

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private SamlIdpMetadataDao idpDao;

	@Inject
	private SamlSpConfigurationDao spDao;

	@Inject
	private SamlHelper samlHelper;

	@Inject
	private AttributeQueryHelper attrQueryHelper;

	@Inject
	private SsoHelper ssoHelper;

	@Inject
	private MetadataHelper metadataHelper;

	@Inject
	private AttributeMapHelper attrHelper;

	@Inject
	private Saml2AssertionService saml2AssertionService;

	@Inject
	private Saml2ResponseValidationService saml2ResponseValidationService;

	@Inject
	private TwoFaService twoFaService;

	@Inject
	private UserLoginInfoDao userLoginInfoDao;

	@Inject
	private HttpRequestContext requestContext;

	@Override
	public Map<String, String> ecpLogin(String eppn, String serviceShortName, String password, String localHostName)
			throws IOException, ServletException, RestInterfaceException {

		SamlUserEntity user = findUser(eppn);
		if (user == null)
			throw new NoUserFoundException("no such user");

		ServiceEntity service = findService(serviceShortName);
		if (service == null)
			throw new NoServiceFoundException("no such service");

		RegistryEntity registry = findRegistry(user, service);
		if (registry == null)
			throw new NoRegistryFoundException("user not registered for service");

		if (password != null
				&& (password.toLowerCase().startsWith("<?xml version") || password.startsWith("<saml2:Assertion "))) {
			return delegateLogin(user, service, registry, password, localHostName);
		} else {
			return ecp(user, service, registry, password, localHostName);
		}
	}

	@Override
	public Map<String, String> ecpLogin(Long regId, String password, String localHostName)
			throws IOException, ServletException, RestInterfaceException {
		RegistryEntity registry = registryDao.fetch(regId);

		if (registry == null) {
			logger.info("No registry found for id {}", regId);
			throw new NoRegistryFoundException("registry unknown");
		}

		MDC.put("userId", "" + registry.getUser().getId());

		/**
		 * TODO also cover OIDC users here
		 */
		if (registry.getUser() instanceof SamlUserEntity) {

			if (password != null && (password.toLowerCase().startsWith("<?xml version")
					|| password.startsWith("<saml2:Assertion "))) {
				return delegateLogin((SamlUserEntity) registry.getUser(), registry.getService(), registry, password,
						localHostName);
			} else {
				return ecp((SamlUserEntity) registry.getUser(), registry.getService(), registry, password,
						localHostName);
			}
		} else if (registry.getUser() instanceof OidcUserEntity) {
			return ecp((OidcUserEntity) registry.getUser(), registry.getService(), registry, password, localHostName);
		} else {
			throw new NoUserFoundException("user type is not implemented");
		}
	}

	private void createLoginInfo(UserEntity user, RegistryEntity registry, UserLoginMethod method,
			UserLoginInfoStatus status) {
		UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
		loginInfo.setUser(user);
		loginInfo.setRegistry(registry);
		loginInfo.setLoginDate(new Date());
		loginInfo.setLoginMethod(method);
		loginInfo.setLoginStatus(status);
		userLoginInfoDao.persist(loginInfo);
	}

	private Map<String, String> localLogin(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String password, String localHostName) throws RestInterfaceException {

		if (password == null || password.equals("")) {
			createLoginInfo(user, registry, UserLoginMethod.LOCAL, UserLoginInfoStatus.FAILED);
			throw new LoginFailedException("Password blank");
		}

		if (service.getServiceProps().containsKey("twofa")
				&& service.getServiceProps().get("twofa").equalsIgnoreCase("enabled")) {

			String separator = ",";
			if (service.getServiceProps().containsKey("twofa_separator")) {
				separator = service.getServiceProps().get("twofa_separator");
			}

			int index = password.lastIndexOf(separator);

			if (index < 2) {
				createLoginInfo(user, registry, UserLoginMethod.TWOFA, UserLoginInfoStatus.FAILED);
				throw new LoginFailedException("Password must contain separator char");
			}

			String twoFa = password.substring(index + 1);
			password = password.substring(0, index);

			try {
				Boolean success = twoFaService.checkToken(user.getIdentity(), twoFa);
				if (!success) {
					logger.info("User {} ({}) failed 2fa authentication", user.getEppn(), user.getId());
					createLoginInfo(user, registry, UserLoginMethod.TWOFA, UserLoginInfoStatus.FAILED);
					throw new LoginFailedException("2fa wrong");
				} else {
					logger.info("User {} ({}) 2fa authentication success", user.getEppn(), user.getId());
					createLoginInfo(user, registry, UserLoginMethod.TWOFA, UserLoginInfoStatus.SUCCESS);
				}
			} catch (TwoFaException e) {
				logger.info("Problems with 2fa authentication", e);
				throw new LoginFailedException("Problems with 2fa authentication");
			}
		}

		if (registry.getRegistryValues().containsKey("userPassword")) {
			logger.debug("userPassword is set on registry. Comparing with given password");
			Boolean match = passwordUtil.comparePassword(password, registry.getRegistryValues().get("userPassword"));
			logger.debug("Passwords match: {}", match);

			if (match) {
				createLoginInfo(user, registry, UserLoginMethod.LOCAL, UserLoginInfoStatus.SUCCESS);

				/*
				 * Update user via AttributeQuery. TODO: Add exceptions for service, if AQ is
				 * not necessary
				 */
				if (user instanceof SamlUserEntity)
					updateSamlUser((SamlUserEntity) user, service, "login-with-service-password");
				else if (user instanceof OidcUserEntity)
					updateOidcUser((OidcUserEntity) user, service, "login-with-service-password");

				List<Object> objectList = checkRules(user, service, registry);
				List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
				List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

				if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
					throw new UnauthorizedException(unauthorizedUserList);
				}

				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				Map<String, String> map = new HashMap<String, String>();
				map.put("eppn", user.getEppn());
				map.put("email", user.getEmail());
				map.put("last_update", df.format(user.getLastUpdate()));

				return map;
			}
		}

		return null;
	}

	private Map<String, String> ecp(OidcUserEntity user, ServiceEntity service, RegistryEntity registry,
			String password, String localHostName) throws RestInterfaceException {

		Map<String, String> attributeMap = localLogin(user, service, registry, password, localHostName);

		if (attributeMap == null) {
			createLoginInfo(user, registry, UserLoginMethod.LOCAL, UserLoginInfoStatus.FAILED);
			throw new LoginFailedException("Local authentication failed (oidc user)");
		}

		return attributeMap;
	}

	private Map<String, String> ecp(SamlUserEntity user, ServiceEntity service, RegistryEntity registry,
			String password, String localHostName) throws RestInterfaceException {

		Map<String, String> attributeMap = localLogin(user, service, registry, password, localHostName);

		if (attributeMap != null) {
			return attributeMap;
		}

		if (service.getServiceProps().containsKey("ecp")
				&& service.getServiceProps().get("ecp").equalsIgnoreCase("disabled")) {
			createLoginInfo(user, registry, UserLoginMethod.LOCAL, UserLoginInfoStatus.FAILED);
			throw new LoginFailedException("Local authentication failed and ecp is disabled (saml user)");
		}

		logger.debug("Attempting ECP Authentication for {} and service {} (regId {})", user.getEppn(),
				service.getShortName(), registry.getId());

		String[] splits = user.getEppn().split("@");

		if (splits.length != 2) {
			throw new NoScopedUsernameException("username must be scoped");
		}
		String username = splits[0];
		String scope = splits[1];

		SamlIdpMetadataEntity idp = user.getIdp();

		if (idp == null) {
			// if idp is not connected with user (legacy) try lookup via scope
			idp = idpDao.findByScope(scope);
		}

		if (idp == null) {
			throw new NoIdpForScopeException("scope unknown");
		}

		try {
			EntityDescriptor idpEntityDesc = samlHelper.unmarshal(idp.getEntityDescriptor(), EntityDescriptor.class);
			if (idpEntityDesc == null) {
				logger.warn("EntityDescriptor for {} is not parsable", idp.getEntityId());
				throw new NoIdpFoundException("IDP metadata are not parseable");
			}

			SingleSignOnService sso = metadataHelper.getSSO(idpEntityDesc, SAMLConstants.SAML2_SOAP11_BINDING_URI);
			if (sso == null) {
				logger.warn("No SOAP Endpoint defined for {}", idp.getEntityId());
				throw new NoEcpSupportException("IDP is not compatible. SOAP ECP Endpoint is missing in metadata");
			}
			String bindingLocation = sso.getLocation();
			String bindingHost = (new URL(bindingLocation)).getHost();
			String hostname = localHostName;
			logger.debug("hostname is {}", hostname);
			List<SamlSpConfigurationEntity> spList = spDao.findByHostname(hostname);

			if (spList.size() != 1) {
				logger.warn("No hostname configured for {}", hostname);
				throw new NoHostnameConfiguredException("No hostname configured");
			}

			SamlSpConfigurationEntity sp = spList.get(0);

			AuthnRequest authnRequest = ssoHelper.buildAuthnRequest(sp.getEntityId(), sp.getEcp(),
					SAMLConstants.SAML2_PAOS_BINDING_URI, idp.getGenericStore(), sso.getLocation());

			MessageContext inbound = new MessageContext();
			MessageContext outbound = new MessageContext();
			outbound.setMessage(authnRequest);

			SOAP11Context soapContext = new SOAP11Context();
			outbound.addSubcontext(soapContext);

			InOutOperationContext inOutContext = new InOutOperationContext(inbound, outbound);

			BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(new AuthScope(bindingHost, 443),
					new UsernamePasswordCredentials(username, password.toCharArray()));

			HttpClient client = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

			PipelineFactoryHttpSOAPClient pf = new PipelineFactoryHttpSOAPClient();
			pf.setHttpClient(client);
			pf.setPipelineFactory(new HttpClientMessagePipelineFactory() {

				@Override
				public HttpClientMessagePipeline newInstance(String pipelineName) {
					final HttpClientResponseSOAP11Decoder decoder = new HttpClientResponseSOAP11Decoder();
					try {
						decoder.getBodyHandler().initialize();
					} catch (ComponentInitializationException e) {
						logger.info("Exception {}", e.getMessage());
					}
					return new BasicHttpClientMessagePipeline(new HttpClientRequestSOAP11Encoder(), decoder);
				}

				@Override
				public HttpClientMessagePipeline newInstance() {
					final HttpClientResponseSOAP11Decoder decoder = new HttpClientResponseSOAP11Decoder();
					try {
						decoder.getBodyHandler().initialize();
					} catch (ComponentInitializationException e) {
						logger.info("Exception {}", e.getMessage());
					}
					return new BasicHttpClientMessagePipeline(new HttpClientRequestSOAP11Encoder(), decoder);
				}
			});

			try {
				pf.send(sso.getLocation(), inOutContext);
			} catch (SOAPException se) {
				logger.info("Login failed for user {} idp {}", username, idp.getEntityId());
				logger.debug("SoapException: {}", se.getMessage());
				if (se.getCause() != null)
					logger.debug("Inner Exception: {}", se.getCause().getMessage());
				createLoginInfo(user, registry, UserLoginMethod.HOME_ORG, UserLoginInfoStatus.FAILED);
				throw new LoginFailedException(se.getMessage());
			}
			Response response = (Response) inOutContext.getInboundMessageContext().getMessage();
			createLoginInfo(user, registry, UserLoginMethod.HOME_ORG, UserLoginInfoStatus.SUCCESS);

			return processResponse(response, idpEntityDesc, service, idp, sp, "ecp");

		} catch (org.opensaml.security.SecurityException e) {
			logger.info("exception at ecp query", e);
			throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
		} catch (DecryptionException e) {
			logger.info("exception at ecp query", e);
			throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
		} catch (IOException e) {
			logger.info("exception at ecp query", e);
			throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
		} catch (SamlAuthenticationException e) {
			logger.info("exception at attribute query", e);
			throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
		}
	}

	private Map<String, String> delegateLogin(SamlUserEntity user, ServiceEntity service, RegistryEntity registry,
			String password, String localHostName) throws RestInterfaceException {

		if (!service.getServiceProps().containsKey("delegate_entities")) {
			throw new NoDelegationConfiguredException("delegation not possible");
		}

		List<String> delegateEntityList = Arrays.asList(service.getServiceProps().get("delegate_entities").split(" "));

		Long delegateAssertionTimeout;

		/* if not configured, use 4 hours */
		if (!service.getServiceProps().containsKey("delegate_assertion_timeout"))
			delegateAssertionTimeout = 4 * 60 * 60 * 1000L;
		else
			delegateAssertionTimeout = Long.parseLong(service.getServiceProps().get("delegate_assertion_timeout"));

		logger.info("Attempting delgated Authentication for {} and service {}", user.getEppn(), service.getShortName());

		Assertion assertion = samlHelper.unmarshal(password, Assertion.class);

		if (assertion == null)
			throw new AssertionException("assertion-invalid");

		if (assertion.getConditions() == null || assertion.getConditions().getAudienceRestrictions() == null
				|| assertion.getConditions().getAudienceRestrictions().size() == 0)
			throw new AssertionException("audience-restriction-missing");

		List<AudienceRestriction> audienceRestrictionList = assertion.getConditions().getAudienceRestrictions();

		if (System.currentTimeMillis()
				- assertion.getConditions().getNotBefore().toEpochMilli() > delegateAssertionTimeout) {
			logger.info("assertion is older than {} ms ({}, {})", delegateAssertionTimeout, user.getEppn(),
					service.getShortName());
			throw new AssertionException("assertion-too-old");
		}

		boolean audienceOk = false;

		for (AudienceRestriction audienceRestriction : audienceRestrictionList) {
			for (Audience audience : audienceRestriction.getAudiences()) {
				if (delegateEntityList.contains(audience.getURI())) {
					logger.debug("Audience hit: {}", audience.getURI());
					audienceOk = true;
					break;
				}
			}
		}

		if (!audienceOk) {
			logger.info("assertion does not match a configured audience (delegate_entities) ({}, {})", user.getEppn(),
					service.getShortName());
			throw new AssertionException("assertion-not-from-configured-audience");
		}

		String hostname = localHostName;
		logger.debug("hostname is {}", hostname);
		List<SamlSpConfigurationEntity> spList = spDao.findByHostname(hostname);

		if (spList.size() != 1) {
			logger.warn("No hostname configured for {}", hostname);
			throw new NoHostnameConfiguredException("No hostname configured");
		}

		SamlSpConfigurationEntity sp = spList.get(0);

		SamlIdpMetadataEntity idp = idpDao.findByEntityId(user.getIdp().getEntityId());

		if (idp == null) {
			logger.info("No IDP found for user {} by entityId {}", user.getEppn(), user.getIdp().getEntityId());
			throw new NoIdpFoundException("idp unknown");
		}

		if (assertion.getIssuer() != null && (!assertion.getIssuer().getValue().equals(idp.getEntityId()))) {
			logger.info("User {} is from idp {}, but assertion from idp {}", user.getEppn(), idp.getEntityId(),
					assertion.getIssuer().getValue());
			throw new AssertionException("wrong-idp-for-user");
		}

		EntityDescriptor idpEntityDesc = samlHelper.unmarshal(idp.getEntityDescriptor(), EntityDescriptor.class);

		if (idpEntityDesc == null) {
			logger.warn("EntityDescriptor for {} is not parsable", idp.getEntityId());
			throw new NoIdpFoundException("IDP metadata are not parseable");
		}

		try {
			logger.debug("Validating Signature for " + assertion.getID());
			saml2ResponseValidationService.validateIdpSignature(assertion, assertion.getIssuer(), idpEntityDesc);
			logger.debug("Validating Signature success for " + assertion.getID());
		} catch (SamlAuthenticationException e) {
			logger.info("Could not validate signature for user {}", user.getEppn());
			throw new AssertionException("assertion-signature-invalid");
		}

		Map<String, List<Object>> attributeMap = saml2AssertionService.extractAttributes(assertion);

		String assertionEppn = attrHelper.getSingleStringFirst(attributeMap, "urn:oid:1.3.6.1.4.1.5923.1.1.1.6");

		if (assertionEppn != null && assertionEppn.equals(user.getEppn())) {

			logger.debug("EPPN from assertion and login match");

			try {

				Response response = attrQueryHelper.query(user, idp, idpEntityDesc, sp);

				return processResponse(response, idpEntityDesc, service, idp, sp, "ecp-delegate");

			} catch (SOAPException e) {
				logger.info("exception at attribute query", e);
				throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
			} catch (MetadataException e) {
				logger.info("exception at attribute query", e);
				throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
			} catch (SecurityException e) {
				logger.info("exception at attribute query", e);
				throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
			} catch (DecryptionException e) {
				logger.info("exception at attribute query", e);
				throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
			} catch (SamlAuthenticationException e) {
				logger.info("exception at attribute query", e);
				throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
			} catch (Exception e) {
				logger.info("exception at attribute query", e);
				throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
			}
		} else {
			if (attributeMap.containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"))
				logger.warn("User tried to login with wrong Eppn from Assertion {} <-> {}", user.getEppn(),
						attributeMap.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
			else
				logger.warn("User {} tried to login with missing Eppn in Assertion", user.getEppn());
			throw new LoginFailedException("Assertion is for wrong EPPN");
		}
	}

	private Map<String, String> processResponse(Response samlResponse, EntityDescriptor idpEntityDescriptor,
			ServiceEntity service, SamlIdpMetadataEntity idp, SamlSpConfigurationEntity spEntity, String caller)
			throws RestInterfaceException, IOException, DecryptionException, SamlAuthenticationException {

		Assertion assertion = saml2AssertionService.processSamlResponse(samlResponse, idp, idpEntityDescriptor,
				spEntity);

		SamlIdentifier samlIdentifier = saml2AssertionService.extractPersistentId(idp, assertion, spEntity, null);
		SamlUserEntity user = saml2AssertionService.resolveUser(samlIdentifier, idp, spEntity.getEntityId(), null);

		if (user == null) {
			throw new UserNotRegisteredException("user not registered in webapp");
		}

		saml2AssertionService.updateUserIdentifier(samlIdentifier, user, spEntity.getEntityId(), null);

		String lastLoginHost = null;
		if (requestContext != null && requestContext.getHttpServletRequest() != null) {
			lastLoginHost = requestContext.getHttpServletRequest().getServerName();
		}

		try {
			user = userUpdater.updateUser(user, assertion, caller, service, null, lastLoginHost);
		} catch (UserUpdateException e) {
			logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
			throw new UserUpdateFailedException("user update failed: " + e.getMessage());
		}

		RegistryEntity registry = findRegistry(user, service);

		if (registry == null) {
			throw new NoRegistryFoundException("No such registry");
		}

		List<Object> objectList = checkRules(user, service, registry);
		List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
		List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

		if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
			throw new UnauthorizedException(unauthorizedUserList);
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Map<String, String> map = new HashMap<String, String>();
		map.put("eppn", user.getEppn());
		map.put("email", user.getEmail());
		map.put("last_update", df.format(user.getLastUpdate()));

		return map;
	}

	private RegistryEntity findRegistry(UserEntity user, ServiceEntity service) {
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);

		if (registry == null) {
			/*
			 * Also check for Lost_access registries. They should also be allowed to be
			 * rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.LOST_ACCESS);
		}

		if (registry == null) {
			/*
			 * Also check for On_hold registries. They should also be allowed to be
			 * rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ON_HOLD);
		}

		return registry;
	}

	private ServiceEntity findService(String serviceShortName) {
		ServiceEntity service = serviceDao.findByShortName(serviceShortName);

		if (service != null) {
			service = serviceDao.findByIdWithServiceProps(service.getId());
		}

		return service;
	}

	private SamlUserEntity findUser(String eppn) {
		SamlUserEntity user = samlUserDao.find(equal("eppn", eppn));

		if (user != null) {
			user = samlUserDao.find(equal(SamlUserEntity_.id, user.getId()), SamlUserEntity_.genericStore,
					SamlUserEntity_.attributeStore);
		}

		return user;
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

	private void updateSamlUser(SamlUserEntity user, ServiceEntity service, String executor)
			throws UserUpdateFailedException {
		// Default expiry Time after which an attrq is issued to IDP in millis
		Long expireTime = 10L * 1000L;
		if (service.getServiceProps() != null && service.getServiceProps().containsKey("attrq_expire_time")) {
			expireTime = Long.parseLong(service.getServiceProps().get("attrq_expire_time"));
		}

		String checkMethod = "standard";
		if (service.getServiceProps() != null && service.getServiceProps().containsKey("attrq_check_method")) {
			checkMethod = service.getServiceProps().get("attrq_check_method");
		}

		if (checkMethod.equalsIgnoreCase("no_attrq")) {
			/*
			 * Don't perform any user update per attribute query or refresh token, if this
			 * is set
			 */

			logger.info("Performing no user update for {} with id {}, as configuration with service",
					new Object[] { user.getEppn(), user.getId() });
		} else if (checkMethod.equalsIgnoreCase("attrq_optional")) {
			/*
			 * Perform user update per attribute query or refresh token, but proceed if it
			 * failed
			 */
			try {
				if ((user.getLastUpdate() != null)
						&& ((System.currentTimeMillis() - user.getLastUpdate().getTime()) < expireTime)) {
					logger.info("Skipping user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });
				} else {
					logger.info("Performing user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });
					user = userUpdater.updateUserFromIdp(user, service, executor, null);
				}
			} catch (UserUpdateException e) {
				logger.warn("Could not update user (attrq is optional, continue with login process) {}: {}",
						e.getMessage(), user.getEppn());
			}
		} else {
			/*
			 * This is the standard case, where attribute query or refresh token is
			 * mandatory
			 */
			try {
				if ((user.getLastUpdate() != null)
						&& ((System.currentTimeMillis() - user.getLastUpdate().getTime()) < expireTime)) {
					logger.info("Skipping user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });
				} else {
					logger.info("Performing user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });

					user = userUpdater.updateUserFromIdp(user, service, executor, null);
				}
			} catch (UserUpdateException e) {
				logger.warn("Could not update user {}: {} (attrq is mandatory, denying access)", e.getMessage(),
						user.getEppn());
				throw new UserUpdateFailedException("user update failed: " + e.getMessage());
			}
		}
	}

	private void updateOidcUser(OidcUserEntity user, ServiceEntity service, String executor)
			throws UserUpdateFailedException {

		logger.info("No update implemented yet for oidc user {} ({})", user.getSubjectId(), user.getIssuer().getName());
	}

	protected void updateIdpStatus(SamlIdpMetadataEntityStatus status, SamlIdpMetadataEntity idpEntity) {
		if (!status.equals(idpEntity.getIdIdpStatus())) {
			idpEntity.setIdIdpStatus(status);
			idpEntity.setLastIdStatusChange(new Date());
		}
	}
}
