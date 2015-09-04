package edu.kit.scc.webreg.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.SingleSignOnService;
import org.opensaml.ws.soap.client.BasicSOAPMessageContext;
import org.opensaml.ws.soap.client.SOAPClientException;
import org.opensaml.ws.soap.client.http.HttpClientBuilder;
import org.opensaml.ws.soap.client.http.HttpSOAPClient;
import org.opensaml.ws.soap.common.SOAPException;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.security.SecurityException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.AssertionException;
import edu.kit.scc.webreg.exc.GenericRestInterfaceException;
import edu.kit.scc.webreg.exc.LoginFailedException;
import edu.kit.scc.webreg.exc.MetadataException;
import edu.kit.scc.webreg.exc.NoDelegationConfiguredException;
import edu.kit.scc.webreg.exc.NoEcpSupportException;
import edu.kit.scc.webreg.exc.NoHostnameConfiguredException;
import edu.kit.scc.webreg.exc.NoIdpForScopeException;
import edu.kit.scc.webreg.exc.NoIdpFoundException;
import edu.kit.scc.webreg.exc.NoItemFoundException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoScopedUsernameException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.exc.UserNotRegisteredException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.exc.UserUpdateFailedException;
import edu.kit.scc.webreg.service.UserLoginService;
import edu.kit.scc.webreg.service.UserUpdateService;
import edu.kit.scc.webreg.service.reg.PasswordUtil;
import edu.kit.scc.webreg.service.saml.AttributeQueryHelper;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.Saml2ResponseValidationService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.SsoHelper;

@Stateless
public class UserLoginServiceImpl implements UserLoginService, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private PasswordUtil passwordUtil;

	@Inject
	private UserDao userDao;
	
	@Inject
	private UserUpdateService userUpdateService;

	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
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

	@Override
	public Map<String, String> ecpLogin(String eppn,
			String serviceShortName,
			String password, String localHostName)
			throws IOException, ServletException, RestInterfaceException {

		UserEntity user = findUser(eppn);
		if (user == null)
			throw new NoUserFoundException("no such user");
		
		ServiceEntity service = findService(serviceShortName);
		if (service == null)
			throw new NoServiceFoundException("no such service");
		
		RegistryEntity registry = findRegistry(user, service);
		if (registry == null)
			throw new NoRegistryFoundException("user not registered for service");
		
		if (password != null && (password.toLowerCase().startsWith("<?xml version") ||
				password.startsWith("<saml2:Assertion "))) {
			return delegateLogin(user, service, registry,
					password, localHostName);
		}
		else {			
			return ecp(user, service, registry, password, localHostName);
		}
	}

	@Override
	public Map<String, String> ecpLogin(Long regId,
			String password, String localHostName)
			throws IOException, ServletException, RestInterfaceException {
		RegistryEntity registry = registryDao.findById(regId);

		if (registry == null) {
			logger.info("No registry found for id {}", regId);
			throw new NoRegistryFoundException("registry unknown");
		}
		
		if (password != null && (password.toLowerCase().startsWith("<?xml version") ||
				password.startsWith("<saml2:Assertion "))) {
			return delegateLogin(registry.getUser(), registry.getService(), registry,
					password, localHostName);
		}
		else {
			return ecp(registry.getUser(), registry.getService(), registry,
				password, localHostName);
		}
	}

	private Map<String, String> ecp(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String password, String localHostName) throws RestInterfaceException {

		if (registry.getRegistryValues().containsKey("userPassword")) {
			logger.debug("userPassword is set on registry. Comparing with given password");
			Boolean match = passwordUtil.comparePassword(password, registry.getRegistryValues().get("userPassword"));
			logger.debug("Passwords match: {}", match);
		}
		
		logger.debug("Attempting ECP Authentication for {} and service {} (regId {})", user.getEppn(), service.getShortName(), registry.getId());

		String[] splits = user.getEppn().split("@");
		
		if (splits.length != 2) {
			throw new NoScopedUsernameException("username must be scoped");
		}
		String username = splits[0];
		String scope = splits[1];
		
		SamlIdpMetadataEntity idp = idpDao.findByScope(scope);

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
			SamlSpConfigurationEntity sp = spDao.findByHostname(hostname);
			
			if (sp == null) {
				logger.warn("No hostname configured for {}", hostname);
				throw new NoItemFoundException("No hostname configured");
			}
			
			AuthnRequest authnRequest = ssoHelper.buildAuthnRequest(sp.getEntityId(), sp.getEcp(),
					SAMLConstants.SAML2_PAOS_BINDING_URI);
			Envelope envelope = attrQueryHelper.buildSOAP11Envelope(authnRequest);
			BasicSOAPMessageContext soapContext = new BasicSOAPMessageContext();
			soapContext.setOutboundMessage(envelope);
			
			HttpClientBuilder clientBuilder = new HttpClientBuilder();
			HttpClient client = clientBuilder.buildClient();
			client.getState().setCredentials(
	                new AuthScope(bindingHost, 443),
	                new UsernamePasswordCredentials(username, password));
			HttpSOAPClient soapClient = new HttpSOAPClient(client, 
					samlHelper.getBasicParserPool());
		
			try {
				soapClient.send(bindingLocation, soapContext);
			} catch (SOAPClientException se) {
				logger.info("Login failed for user {} idp {}", username, idp.getEntityId());
				throw new LoginFailedException(se.getMessage());
			}
			Envelope returnEnvelope = (Envelope) soapContext.getInboundMessage();
			Response response = 
					attrQueryHelper.getResponseFromEnvelope(returnEnvelope);

			return processResponse(response, idpEntityDesc, service, idp, sp, "ecp");

		} catch (SOAPException e) {
			logger.info("exception at ecp query", e);
			throw new GenericRestInterfaceException("an error occured: " + e.getMessage());
		} catch (SecurityException e) {
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

	private Map<String, String> delegateLogin(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String password, String localHostName)
			throws IOException, ServletException, RestInterfaceException {
		
		if (! service.getServiceProps().containsKey("delegate_entities")) {
			throw new NoDelegationConfiguredException("delegation not possible");
		}
		
		List<String> delegateEntityList = Arrays.asList(service.getServiceProps().get("delegate_entities").split(" "));
		
		Long delegateAssertionTimeout;
		
		/* if not configured, use 4 hours */
		if (! service.getServiceProps().containsKey("delegate_assertion_timeout"))
			delegateAssertionTimeout = 4 * 60 * 60 * 1000L;
		else
			delegateAssertionTimeout = Long.parseLong(service.getServiceProps().get("delegate_assertion_timeout"));
			
		logger.info("Attempting delgated Authentication for {} and service {}", user.getEppn(), service.getShortName());

		Assertion assertion = samlHelper.unmarshal(password, Assertion.class);
		
		if (assertion == null)
			throw new AssertionException("assertion-invalid");

		if (assertion.getConditions() == null ||
				assertion.getConditions().getAudienceRestrictions() == null ||
				assertion.getConditions().getAudienceRestrictions().size() == 0)
			throw new AssertionException("audience-restriction-missing");
			
		List<AudienceRestriction> audienceRestrictionList = assertion.getConditions().getAudienceRestrictions();
		
		if (System.currentTimeMillis() - assertion.getConditions().getNotBefore().getMillis() > delegateAssertionTimeout) {
			logger.info("assertion is older than {} ms ({}, {})", delegateAssertionTimeout, user.getEppn(), service.getShortName());
			throw new AssertionException("assertion-too-old");
		}
		
		boolean audienceOk = false;
		
		for (AudienceRestriction audienceRestriction : audienceRestrictionList) {
			for (Audience audience : audienceRestriction.getAudiences()) {
				if (delegateEntityList.contains(audience.getAudienceURI())) {
					logger.debug("Audience hit: {}", audience.getAudienceURI());
					audienceOk = true;
					break;
				}
			}
		}
		
		if (! audienceOk) { 
			logger.info("assertion does not match a configured audience (delegate_entities) ({}, {})", user.getEppn(), service.getShortName());
			throw new AssertionException("assertion-not-from-configured-audience");
		}
		
		String hostname = localHostName;
		logger.debug("hostname is {}", hostname);
		SamlSpConfigurationEntity sp = spDao.findByHostname(hostname);
		
		if (sp == null) {
			logger.warn("No hostname configured for {}", hostname);
			throw new NoHostnameConfiguredException("No hostname configured");
		}
		
		SamlIdpMetadataEntity idp = idpDao.findByEntityId(user.getIdp().getEntityId());

		if (idp == null) {
			logger.info("No IDP found for user {} by entityId {}", user.getEppn(), user.getIdp().getEntityId());
			throw new NoIdpFoundException("idp unknown");
		}

		if (assertion.getIssuer() != null && 
				(! assertion.getIssuer().getValue().equals(idp.getEntityId()))) {
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
		
		if (assertionEppn != null &&
				assertionEppn.equals(user.getEppn())) {
			
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
			}
		}
		else {
			if (attributeMap.containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"))
				logger.warn("User tried to login with wrong Eppn from Assertion {} <-> {}", user.getEppn(), attributeMap.get("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
			else
				logger.warn("User {} tried to login with missing Eppn in Assertion", user.getEppn());
			throw new LoginFailedException("Assertion is for wrong EPPN");
		}
	}
	private Map<String, String> processResponse(Response samlResponse, EntityDescriptor idpEntityDescriptor,
			ServiceEntity service, SamlIdpMetadataEntity idp, SamlSpConfigurationEntity spEntity, String caller) 
					throws RestInterfaceException, IOException, DecryptionException, SamlAuthenticationException {
		
		Assertion assertion = saml2AssertionService.processSamlResponse(samlResponse, idp, idpEntityDescriptor, spEntity);

		String persistentId = saml2AssertionService.extractPersistentId(assertion, spEntity);
		
		UserEntity user = userDao.findByPersistentWithRoles(spEntity.getEntityId(), 
				idp.getEntityId(), persistentId);
	
		if (user == null) {
			throw new UserNotRegisteredException("user not registered in webapp");
		}
		
		try {
			user = userUpdateService.updateUser(user, assertion, caller, service);
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
		
		if (unauthorizedUserList.size() > 0 || overrideAccessList.size() == 0) {
			throw new UnauthorizedException(unauthorizedUserList);
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Map<String, String> map = new HashMap<String, String>();
		map.put("eppn", user.getEppn());
		map.put("email", user.getEmail());
		map.put("last_update",  df.format(user.getLastUpdate()));
		
		return map;
	}
	
	private RegistryEntity findRegistry(UserEntity user, ServiceEntity service) {
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null) {
			/*
			 * Also check for Lost_access registries. They should also be allowed to be rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.LOST_ACCESS);
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

	private UserEntity findUser(String eppn) {
		UserEntity user = userDao.findByEppn(eppn);

		if (user != null) {
			user = userDao.findByIdWithStore(user.getId());
		}

		return user;
	}

	private List<Object> checkRules(UserEntity user, ServiceEntity service, RegistryEntity registry) {
		List<Object> objectList;
		
		if (service.getAccessRule() == null) {
			objectList = knowledgeSessionService.checkRule("default", "permitAllRule", "1.0.0", user, service, registry, "user-self", false);
		}
		else {
			BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();
			if (rulePackage != null) {
				objectList = knowledgeSessionService.checkRule(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
					rulePackage.getKnowledgeBaseVersion(), user, service, registry, "user-self", false);
			}
			else {
				throw new IllegalStateException("checkServiceAccess called with a rule (" +
							service.getAccessRule().getName() + ") that has no rulePackage");
			}
		}

		return objectList;
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
