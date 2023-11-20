package edu.kit.scc.webreg.sec;

import java.io.IOException;

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

	@Inject
	private Saml2IdpMetadataHandler metadataHandler;
	
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

		if (path != null && path.endsWith("/redirect")) {
			logger.debug("Executing Redirect Handler");
			redirectHandler.service(request, response);
			return;
		}
		else if (path != null && path.endsWith("/redirect/response")) {
			logger.debug("Executing Redirect Response Handler");
			redirectResponseHandler.service(request, response);
			return;
		}
		else if (path != null && path.endsWith("/metadata")) {
			logger.debug("Executing Metadata Handler");
			metadataHandler.service(request, response);
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
