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

import edu.kit.scc.webreg.service.oidc.client.OidcClientRedirectService;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@WebServlet(urlPatterns = { "/rpoidc/login" })
public class OidcClientRedirectHandlerServlet implements Servlet {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject
	private OidcClientRedirectService redirectService;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		if (session == null || session.getOidcRelyingPartyId() == null) {
			logger.debug("Client session from {} not established. Sending client back to welcome page",
					request.getRemoteAddr());
			response.sendRedirect("/welcome/index.xhtml");
			return;
		}
		
		try {
			redirectService.redirectClient(session.getOidcRelyingPartyId(), request, response);
		} catch (OidcAuthenticationException e) {
			throw new ServletException("Problems encountered: " + e.getMessage());
		}
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
