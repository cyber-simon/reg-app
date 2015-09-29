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

import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.SamlAAConfigurationService;
import edu.kit.scc.webreg.service.SamlSpConfigurationService;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@WebServlet(urlPatterns = {"/Shibboleth.sso/*", "/saml/*"})
public class Saml2DispatcherServlet implements Servlet {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject 
	private SamlSpConfigurationService spConfigService;

	@Inject 
	private SamlAAConfigurationService aaConfigService;

	@Inject
	private Saml2AttributeQueryServlet attributeQueryServlet;
	
	@Inject
	private Saml2PostHandlerServlet postHandlerServlet;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
	}

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
		
		if (spConfig != null && spConfig.getAcs() != null &&
				spConfig.getAcs().endsWith(context + path)) {
			logger.debug("Executing POST Handler for entity {}", spConfig.getEntityId());
			postHandlerServlet.service(servletRequest, servletResponse, spConfig);
			return;
		}

		SamlAAConfigurationEntity aaConfig = aaConfigService.findByHostname(request.getServerName());
		
		if (aaConfig != null && aaConfig.getAq() != null &&
				aaConfig.getAq().endsWith(context + path)) {
			logger.debug("Executing AttributeQuery Handler for entity {}", aaConfig.getEntityId());
			attributeQueryServlet.service(servletRequest, servletResponse, aaConfig);
			return;
		}

		logger.info("No matching servlet for context '{}' path '{}'", context, path);
		
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
