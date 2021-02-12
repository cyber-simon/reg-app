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
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;

import edu.kit.scc.webreg.util.ViewIds;

@Named
@WebServlet(urlPatterns = {"/logout/*"})
public class LogoutServlet implements Servlet {

	@Inject
	private Logger logger;

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
		
		logger.debug("logout request context '{}' path '{}'", context, path);

		HttpSession session = request.getSession(false);
		if(session != null) {
			session.invalidate();
		}
		String redirect = request.getParameter("redirect");
		if (redirect != null && (! redirect.equals(""))) {
			if (redirect.equalsIgnoreCase("delete"))
				response.sendRedirect(ViewIds.DELETE_ALL_PERSONAL_DATA_DONE);
			else if (redirect.equalsIgnoreCase("local_logout"))
				response.sendRedirect(ViewIds.LOCAL_LOGOUT_DONE);
			else
				response.sendRedirect(ViewIds.INDEX_USER);
		}
		else {
			response.sendRedirect(ViewIds.INDEX_USER);
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
