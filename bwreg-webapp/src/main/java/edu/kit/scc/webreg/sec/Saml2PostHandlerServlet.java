/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.sec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.security.SecurityException;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.SamlIdpMetadataService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserUpdateService;
import edu.kit.scc.webreg.service.saml.Saml2AssertionService;
import edu.kit.scc.webreg.service.saml.Saml2DecoderService;
import edu.kit.scc.webreg.service.saml.SamlHelper;
import edu.kit.scc.webreg.util.SessionManager;

@ApplicationScoped
public class Saml2PostHandlerServlet {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject
	private UserUpdateService userUpdateService;
	
	@Inject
	private UserService userService;
	
	@Inject
	private Saml2DecoderService saml2DecoderService;
	
	@Inject
	private Saml2AssertionService saml2AssertionService;
	
	@Inject
	private SamlHelper samlHelper;
		
	@Inject 
	private SamlIdpMetadataService idpService;
	
	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@Inject
	private ApplicationConfig appConfig;
	
	public void service(ServletRequest servletRequest, ServletResponse servletResponse, SamlSpConfigurationEntity spConfig)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		if (session == null || session.getIdpId() == null || session.getSpId() == null) {
			logger.debug("Client session from {} not established. Sending client back to welcome page",
					request.getRemoteAddr());
			response.sendRedirect("/welcome/index.xhtml");
			return;
		}
		
		logger.debug("attempAuthentication, Consuming SAML Assertion");
		
		try {
			SamlIdpMetadataEntity idpEntity = idpService.findById(session.getIdpId());
			EntityDescriptor idpEntityDescriptor = samlHelper.unmarshal(
					idpEntity.getEntityDescriptor(), EntityDescriptor.class);
		
			Response samlResponse = saml2DecoderService.decodePostMessage(request);

			Assertion assertion = saml2AssertionService.processSamlResponse(samlResponse, idpEntity, idpEntityDescriptor, spConfig);
			
			String persistentId = saml2AssertionService.extractPersistentId(assertion, spConfig);
			
			logger.debug("Storing relevant SAML data in session");
			session.setPersistentId(persistentId);
			Map<String, List<Object>> attributeMap = saml2AssertionService.extractAttributes(assertion);
			session.setAttributeMap(attributeMap);
			
			UserEntity user = userService.findByPersistentWithRoles(spConfig.getEntityId(), 
						idpEntity.getEntityId(), persistentId);
			
			if (user == null) {
				logger.info("New User detected, sending to register Page");
				// Role -1 is for new users
				session.addRole(-1L);
				response.sendRedirect("/register/register.xhtml");
				return;
			}
			
			String userLoginRule = appConfig.getConfigValue("user_login_rule");
			
			if (userLoginRule == null || "".equals(userLoginRule)) {
				userLoginRule = "default:permitAllRule:1.0.0";
			}
			
			logger.debug("Checking User login rule {}", userLoginRule);
	    	long start = System.currentTimeMillis();

			knowledgeSessionService.checkRule(userLoginRule, user, attributeMap, assertion, 
					idpEntity, idpEntityDescriptor, spConfig);
			
	    	long end = System.currentTimeMillis();
	    	logger.debug("Rule processing took {} ms", end - start);

	    	logger.debug("Updating user {}", persistentId);
			
			try {
				user = userUpdateService.updateUser(user, attributeMap, "web-sso");
			} catch (UserUpdateException e) {
				logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
				throw new SamlAuthenticationException(e.getMessage());
			}
			
			session.setUserId(user.getId());
			session.setTheme(user.getTheme());
			session.setLocale(user.getLocale());
			
			if (session.getOriginalRequestPath() != null)
				response.sendRedirect(session.getOriginalRequestPath());
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
		}
	}
}
