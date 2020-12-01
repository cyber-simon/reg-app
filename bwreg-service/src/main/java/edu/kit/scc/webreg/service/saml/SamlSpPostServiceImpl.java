package edu.kit.scc.webreg.service.saml;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.messaging.decoder.MessageDecodingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
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
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.impl.UserUpdater;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;

@Stateless
public class SamlSpPostServiceImpl implements SamlSpPostService {

	@Inject
	private Logger logger;

	@Inject 
	private SamlIdpMetadataDao idpDao;
	
	@Inject
	private SamlUserDao userDao;
	
	@Inject
	private UserLoginInfoDao userLoginInfoDao;

	@Inject
	private UserUpdater userUpdater;
	
	@Inject
	private SamlHelper samlHelper;
	
	@Inject
	private Saml2DecoderService saml2DecoderService;
	
	@Inject
	private Saml2AssertionService saml2AssertionService;	

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Inject
	private SessionManager session;
	
	@Override
	public void consumePost(HttpServletRequest request, HttpServletResponse response, 
			SamlSpConfigurationEntity spConfig) throws Exception {
		try {
			SamlIdpMetadataEntity idpEntity = idpDao.findById(session.getIdpId());
			EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(
					idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		
			Assertion assertion;
			String persistentId;
			try {
				Response samlResponse = saml2DecoderService.decodePostMessage(request);

				assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, idpEntityDescriptor, spConfig);
				
				persistentId = saml2AssertionService.extractPersistentId(assertion, spConfig);
			} catch (Exception e1) {
				/*
				 * Catch Exception here for a probably faulty IDP. Register Exception and rethrow.
				 */
				if (! SamlIdpMetadataEntityStatus.FAULTY.equals(idpEntity.getIdIdpStatus())) {
					idpEntity.setIdIdpStatus(SamlIdpMetadataEntityStatus.FAULTY);
					idpEntity.setLastIdStatusChange(new Date());
				}
				throw e1;
			}
			
			if (! SamlIdpMetadataEntityStatus.GOOD.equals(idpEntity.getIdIdpStatus())) {
				idpEntity.setIdIdpStatus(SamlIdpMetadataEntityStatus.GOOD);
				idpEntity.setLastIdStatusChange(new Date());
			}

			Map<String, List<Object>> attributeMap = saml2AssertionService.extractAttributes(assertion);

			SamlUserEntity user = userDao.findByPersistent(spConfig.getEntityId(), 
						idpEntity.getEntityId(), persistentId);

			if (user != null) {
				MDC.put("userId", "" + user.getId());
			}
			
			String userLoginRule = appConfig.getConfigValue("user_login_rule");
			
			if (userLoginRule != null && (! "".equals(userLoginRule))) {
				logger.debug("Checking User login rule {}", userLoginRule);
		    	long start = System.currentTimeMillis();

				knowledgeSessionService.checkRule(userLoginRule, user, attributeMap, assertion, 
						idpEntity, idpEntityDescriptor, spConfig);
				
		    	long end = System.currentTimeMillis();
		    	logger.debug("Rule processing took {} ms", end - start);
			}
			
			if (user == null) {
				logger.info("New User detected, sending to register Page");

				// Store SAML Data temporarily in Session
				logger.debug("Storing relevant SAML data in session");
				session.setPersistentId(persistentId);
				session.setAttributeMap(attributeMap);				
				
				response.sendRedirect("/register/register.xhtml");
				return;
			}
			
	    	logger.debug("Updating user {}", persistentId);
			
			try {
				user = userUpdater.updateUser(user, assertion, "web-sso");
			} catch (UserUpdateException e) {
				logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
				throw new SamlAuthenticationException(e.getMessage());
			}
			
			session.setIdentityId(user.getIdentity().getId());
			session.setLoginTime(Instant.now());
			session.setTheme(user.getTheme());
			session.setLocale(user.getLocale());
			
			UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
			loginInfo.setUser(user);
			loginInfo.setLoginDate(new Date());
			loginInfo.setLoginMethod(UserLoginMethod.HOME_ORG);
			loginInfo.setLoginStatus(UserLoginInfoStatus.SUCCESS);
			loginInfo.setFrom(request.getRemoteAddr());
			loginInfo = userLoginInfoDao.persist(loginInfo);
			
			if (session.getOriginalRequestPath() != null) {
				String orig = session.getOriginalRequestPath();
				session.setOriginalRequestPath(null);
				response.sendRedirect(orig);
			}
			else
				response.sendRedirect("/index.xhtml");

			return;

		} catch (MessageDecodingException e) {
			throw new ServletException("Authentication problem", e);
		} catch (SecurityException e) {
			throw new ServletException("Authentication problem", e);
		} catch (DecryptionException e) {
			throw new ServletException("Authentication problem", e);
		} catch (SamlAuthenticationException e) {
			throw new ServletException("Authentication problem", e);
		} catch (ComponentInitializationException e) {
			throw new ServletException("Authentication problem", e);
		}		
	}
}
