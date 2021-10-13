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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import edu.kit.scc.webreg.service.saml.SamlIdpService;
import edu.kit.scc.webreg.service.saml.exc.SamlAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;

@ApplicationScoped
public class Saml2IdpRedirectResponseHandler {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;

	@Inject
	private SamlIdpService samlIdpService;

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		if (session.getAuthnRequestId() == null) {
			logger.warn("No AuthnRequestId set in session. Cannot continue");
			return;
		}
		
		if (session.getIdentityId() == null) {
			logger.warn("No UserId set in session. Cannot continue");
			return;
		}
		
		try {
			String redirect = samlIdpService.resumeAuthnRequest(session.getAuthnRequestId(), session.getIdentityId(), 
								session.getAuthnRequestIdpConfigId(), session.getAuthnRequestRelayState(), response);
			
			if (redirect != null) {
				session.setOriginalRequestPath(request.getRequestURI());
				response.sendRedirect(redirect);
			}
		} catch (SamlAuthenticationException e) {
			throw new ServletException(e);
		}
	}
}
