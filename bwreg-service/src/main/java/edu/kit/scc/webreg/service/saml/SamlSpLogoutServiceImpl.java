package edu.kit.scc.webreg.service.saml;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.messaging.context.SAMLEndpointContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.binding.encoding.impl.HTTPRedirectDeflateEncoder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.slf4j.Logger;
import org.slf4j.MDC;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dao.SamlUserDao;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntityStatus;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.impl.UserUpdater;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;

@Stateless
public class SamlSpLogoutServiceImpl implements SamlSpLogoutService {

	@Inject
	private Logger logger;

	@Inject 
	private SamlIdpMetadataDao idpDao;
	
	@Inject
	private SamlUserDao userDao;
	
	@Inject
	private SamlHelper samlHelper;
	
	@Inject
	private MetadataHelper metadataHelper;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private SessionManager session;
	
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
		
		EntityDescriptor entityDesc = samlHelper.unmarshal(
				idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		SingleLogoutService slo = metadataHelper.getSLO(entityDesc, SAMLConstants.SAML2_REDIRECT_BINDING_URI);

		LogoutRequest logoutRequest = samlHelper.create(LogoutRequest.class, LogoutRequest.DEFAULT_ELEMENT_NAME);
		logoutRequest.setID(samlHelper.getRandomId());
		logoutRequest.setVersion(SAMLVersion.VERSION_20);
		logoutRequest.setIssueInstant(new DateTime());

		NameID nameId = samlHelper.create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
		nameId.setFormat(NameID.PERSISTENT);
		nameId.setValue(user.getPersistentId());
		logoutRequest.setNameID(nameId);

		Issuer issuer = samlHelper.create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
		issuer.setValue(user.getPersistentSpId());
		logoutRequest.setIssuer(issuer);
		
		logger.debug("Logout Request: {}", samlHelper.prettyPrint(logoutRequest));
		
		/**
		 * Need to sign logout message. Doesn't get accepted unsigned.
		 */
		
		MessageContext<SAMLObject> messageContext = new MessageContext<SAMLObject>();
		messageContext.setMessage(logoutRequest);
		SAMLPeerEntityContext entityContext = new SAMLPeerEntityContext();
		entityContext.setEntityId(idpEntity.getEntityId());
		SAMLEndpointContext endpointContext = new SAMLEndpointContext();
		endpointContext.setEndpoint(slo);
		entityContext.addSubcontext(endpointContext);
		messageContext.addSubcontext(entityContext);
		
		HTTPRedirectDeflateEncoder encoder = new HTTPRedirectDeflateEncoder();
		encoder.setHttpServletResponse(response);
		encoder.setMessageContext(messageContext);
		encoder.initialize();
		encoder.prepareContext();
		encoder.encode();
	}
}
