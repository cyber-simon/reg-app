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

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.service.saml.SamlSpPostService;
import edu.kit.scc.webreg.session.SessionManager;

@ApplicationScoped
public class Saml2PostHandler {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject
	private SamlSpPostService spPostService;
	
	public void service(HttpServletRequest request, HttpServletResponse response, SamlSpConfigurationEntity spConfig)
			throws ServletException, IOException {

		if (session == null || session.getIdpId() == null || session.getSpId() == null) {
			logger.debug("Client session from {} not established. Sending client back to welcome page",
					request.getRemoteAddr());
			response.sendRedirect("/welcome/index.xhtml");
			return;
		}
		
		logger.debug("attempAuthentication, Consuming SAML Assertion");
		
		try {
			spPostService.consumePost(request, response, spConfig);
			
		} catch (Exception e) {
			throw new ServletException("Authentication problem", e);
		}
	}
}
