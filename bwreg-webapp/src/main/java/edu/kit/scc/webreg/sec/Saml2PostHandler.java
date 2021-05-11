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

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.service.saml.SamlSpPostService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@WebServlet(urlPatterns = {"/Shibboleth.sso/SAML2/POST", "/saml/sp/post"})
public class Saml2PostHandler implements Servlet {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject 
	private SamlSpConfigurationService spConfigService;

	@Inject
	private SamlSpPostService spPostService;

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String context = request.getServletContext().getContextPath();
		String path = request.getRequestURI().substring(
				context.length());
		
		logger.debug("Dispatching request context '{}' path '{}'", context, path);
		
		SamlSpConfigurationEntity spConfig = spConfigService.findByHostname(request.getServerName());
		
		if (spConfig != null) {
			logger.debug("Executing POST Handler for entity {}", spConfig.getEntityId());
			service(request, response, spConfig);
		}
	}
	
	private void service(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig)
			throws ServletException, IOException {

		if (session == null || session.getIdpId() == null || session.getSpId() == null) {
			logger.debug("Client session from {} not established. Sending client back to welcome page",
					request.getRemoteAddr());
			response.sendRedirect("/welcome/index.xhtml");
			return;
		}
		
		StringBuffer debugLog = null;
		if (session.getOriginalRequestPath() != null && session.getOriginalRequestPath().startsWith("/idp-debug-login/")) {
			debugLog = new StringBuffer();
			debugLog.append("Starting debug log for login process...\n");
			logger.debug("attempAuthentication, Client debug is on");
		}
		
		logger.debug("attempAuthentication, Consuming SAML Assertion");
		
		try {
			spPostService.consumePost(request, response, spConfig, debugLog);
	
			if (debugLog != null) {
				request.setAttribute("_debugLog", debugLog.toString());
				request.getServletContext().getRequestDispatcher("/idp-debug-login/").forward(request, response);
			}
			
		} catch (Exception e) {
			if (debugLog != null) {
				debugLog.append("Exception: ").append(e.getMessage()).append("\n");
				if (e.getCause() != null) {
					debugLog.append("Cause: ").append(e.getMessage()).append("\n");
				}
				
				request.setAttribute("_debugLog", debugLog.toString());
				request.getServletContext().getRequestDispatcher("/idp-debug-login/").forward(request, response);
			}
			else {
				throw new ServletException("Authentication problem", e);
			}
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		
	}
	
	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public String getServletInfo() {
		return null;
	}

	@Override
	public void destroy() {
	}	
}
