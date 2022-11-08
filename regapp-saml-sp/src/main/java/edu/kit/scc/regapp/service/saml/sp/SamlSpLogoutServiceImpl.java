package edu.kit.scc.regapp.service.saml.sp;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.messaging.SAMLMessageSecuritySupport;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.RoleDescriptorCriterion;
import org.opensaml.saml.saml2.binding.decoding.impl.HTTPRedirectDeflateDecoder;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.security.impl.SAMLMetadataSignatureSigningParametersResolver;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.SignatureSigningParameters;
import org.opensaml.xmlsec.config.impl.DefaultSecurityConfigurationBootstrap;
import org.opensaml.xmlsec.context.SecurityParametersContext;
import org.opensaml.xmlsec.criterion.SignatureSigningConfigurationCriterion;
import org.opensaml.xmlsec.impl.BasicSignatureSigningConfiguration;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlSpConfigurationDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.MetadataHelper;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.service.saml.exc.NoHostnameConfiguredException;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;

@Stateless
public class SamlSpLogoutServiceImpl implements SamlSpLogoutService {

	@Inject
	private Logger logger;

	@Inject 
	private SamlIdpMetadataDao idpDao;
	
	@Inject
	private SamlSpConfigurationDao spConfigDao;
	
	@Inject
	private SamlUserDao userDao;
	
	@Inject
	private SamlHelper samlHelper;
	
	@Inject
	private MetadataHelper metadataHelper;
	
	@Inject
	private CryptoHelper cryptoHelper;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private SessionManager session;

	@Override
	public void consumeRedirectLogout(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig)
			throws Exception {
		HTTPRedirectDeflateDecoder decoder = new HTTPRedirectDeflateDecoder();
		decoder.setHttpServletRequest(request);

		decoder.initialize();
		decoder.decode();

		SAMLObject obj = decoder.getMessageContext().getMessage();
		logger.debug("Message decoded: {}", obj);
	}

	@Override
	public void redirectLogout(HttpServletRequest request, HttpServletResponse response, Long userId)
			throws Exception {

		logger.debug("Init SAML redirect logout for {}", userId);
		
		if (! session.getLoggedInUserList().contains(userId)) {
			logger.warn("SAML logout for userId {}, not in logged in list", userId);
			return;
		}
		
		UserEntity tempUser = userDao.findById(userId);
		if (! (tempUser instanceof SamlUserEntity)) {
			logger.warn("SAML logout for userId {}: User is not SAML Type, but {}", tempUser.getClass().getName());
			return;
		}
		
		SamlUserEntity user = (SamlUserEntity) tempUser;
		SamlIdpMetadataEntity idpEntity = user.getIdp();
		
		List<SamlSpConfigurationEntity> spList = spConfigDao.findByHostname(request.getLocalName());
		
		if (spList.size() != 1) {
			logger.warn("No hostname configured for {}", request.getLocalName());
			throw new NoHostnameConfiguredException("No hostname configured");
		}

		SamlSpConfigurationEntity spEntity = spList.get(0);
		
		EntityDescriptor entityDesc = samlHelper.unmarshal(
				idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		SingleLogoutService slo = metadataHelper.getSLO(entityDesc, SAMLConstants.SAML2_REDIRECT_BINDING_URI);

		LogoutRequest logoutRequest = samlHelper.create(LogoutRequest.class, LogoutRequest.DEFAULT_ELEMENT_NAME);
		logoutRequest.setID(samlHelper.getRandomId());
		logoutRequest.setVersion(SAMLVersion.VERSION_20);
		logoutRequest.setIssueInstant(new DateTime());
		logoutRequest.setDestination(slo.getLocation());

		NameID nameId = samlHelper.create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameId.setFormat(NameID.PERSISTENT);
		nameId.setValue(user.getPersistentId());
		logoutRequest.setNameID(nameId);

		Issuer issuer = samlHelper.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(user.getPersistentSpId());
		logoutRequest.setIssuer(issuer);
		
		logger.debug("Logout Request: {}", samlHelper.prettyPrint(logoutRequest));
		
		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();
		messageContext.setMessage(logoutRequest);
		SAMLPeerEntityContext entityContext = new SAMLPeerEntityContext();
		entityContext.setEntityId(idpEntity.getEntityId());
		SAMLEndpointContext endpointContext = new SAMLEndpointContext();
		endpointContext.setEndpoint(slo);
		entityContext.addSubcontext(endpointContext);
		messageContext.addSubcontext(entityContext);

		/**
		 * Need to sign logout message. Doesn't get accepted unsigned.
		 */

		PrivateKey privateKey;
		X509Certificate publicKey;
		try {
			privateKey = cryptoHelper.getPrivateKey(spEntity.getPrivateKey());
			publicKey = cryptoHelper.getCertificate(spEntity.getCertificate());
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
		criteriaSet.add(new RoleDescriptorCriterion(entityDesc.getIDPSSODescriptor(SAMLConstants.SAML20P_NS)));
		SAMLMetadataSignatureSigningParametersResolver smsspr = new SAMLMetadataSignatureSigningParametersResolver();
		
		SignatureSigningParameters ssp = smsspr.resolveSingle(criteriaSet);
		logger.debug("Resolved algo {} for signing", ssp.getSignatureAlgorithm());
		SecurityParametersContext securityContext = new SecurityParametersContext();
		securityContext.setSignatureSigningParameters(ssp);
		messageContext.addSubcontext(securityContext);

		SAMLMessageSecuritySupport.signMessage(messageContext);
		
		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		encoder.setHttpServletResponse(response);
		encoder.setMessageContext(messageContext);
		encoder.initialize();
		encoder.prepareContext();
		encoder.encode();
	}
}
