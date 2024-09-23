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

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity;
import edu.kit.scc.webreg.service.SamlAAConfigurationService;
import edu.kit.scc.webreg.service.saml.SamlAttributeQueryService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Named
@WebServlet(urlPatterns = { "/Shibboleth.sso/SAML2/AttributeQuery", "/saml/sp/attribute-query" })
public class Saml2AttributeQueryHandler implements Servlet {

	@Inject
	private Logger logger;

	@Inject
	private SamlAttributeQueryService aqService;

	@Inject
	private SamlAAConfigurationService aaConfigService;

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String context = request.getServletContext().getContextPath();
		String path = request.getRequestURI().substring(context.length());

		logger.debug("Dispatching request context '{}' path '{}'", context, path);

		SamlAAConfigurationEntity aaConfig = aaConfigService.findByHostname(request.getServerName());

		if (aaConfig != null && aaConfig.getAq() != null && aaConfig.getAq().endsWith(context + path)) {
			logger.debug("Executing AttributeQuery Handler for entity {}", aaConfig.getEntityId());
			aqService.consumeAttributeQuery(request, response, aaConfig);
			return;
		}

		logger.info("No matching servlet for context '{}' path '{}'", context, path);

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
