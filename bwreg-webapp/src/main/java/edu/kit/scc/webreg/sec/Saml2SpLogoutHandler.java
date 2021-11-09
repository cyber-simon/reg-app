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
import edu.kit.scc.webreg.service.saml.SamlSpLogoutService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@WebServlet(urlPatterns = {"/Shibboleth.sso/SLO/Redirect", "/saml/sp/logout/redirect"})
public class Saml2SpLogoutHandler implements Servlet {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject 
	private SamlSpConfigurationService spConfigService;

	@Inject
	private SamlSpLogoutService samlLogoutService;

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String context = request.getServletContext().getContextPath();
		String path = request.getRequestURI().substring(
				context.length());
		
		logger.debug("Dispatching request context '{}' path '{}'", context, path);
		
		List<SamlSpConfigurationEntity> spConfigList = spConfigService.findByHostname(request.getServerName());
		
		if (spConfigList.size() != 1) {
			throw new ServletException("Logout only works with one SP per host at the moment");
		}
		
		logger.debug("Executing POST Handler for entity {}", spConfigList.get(0).getEntityId());
		service(request, response, spConfigList.get(0));
	}
	
	private void service(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig)
			throws ServletException, IOException {

		if (session == null || session.getIdpId() == null || session.getSpId() == null) {
			logger.debug("Client session from {} not established. Sending client back to welcome page",
					request.getRemoteAddr());
			response.sendRedirect("/welcome/index.xhtml");
			return;
		}
		
		logger.debug("attemp Logout, Consuming SAML Logout Request");
		
		try {
			samlLogoutService.consumeRedirectLogout(request, response, spConfig);
	
		} catch (Exception e) {
			throw new ServletException("Authentication problem", e);
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
