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

@Named
@WebServlet(urlPatterns = {"/saml/idp/*"})
public class SamlIdpDispatcherServlet implements Servlet {

	@Inject
	private Logger logger;
	
	@Inject
	private Saml2IdpRedirectHandler redirectHandler;

	@Inject
	private Saml2IdpRedirectResponseHandler redirectResponseHandler;

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

		if ("/saml/idp/redirect".equals(path)) {
			logger.debug("Executing Redirect Handler");
			redirectHandler.service(request, response);
			return;
		}
		else if ("/saml/idp/redirect/response".equals(path)) {
			logger.debug("Executing Redirect Response Handler");
			redirectResponseHandler.service(request, response);
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
