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
import java.security.Security;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.service.AdminUserService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.reg.PasswordUtil;
import edu.kit.scc.webreg.session.SessionManager;

@Named
@WebFilter(urlPatterns = {"/*"})
public class SecurityFilter implements Filter {

	public static final String ADMIN_USER_ID = "_admin_user_id";
	public static final String USER_ID = "_user_id";
	public static final String DIRECT_USER_ID = "_direct_user_id";
	public static final String DIRECT_USER_PW = "_direct_user_pw";
	
	@Inject 
	private Logger logger;
	
	@Inject
	private SessionManager session;
	
	@Inject
	private AccessChecker accessChecker;
	
	@Inject
	private RoleService roleService;
	
	@Inject
	private AdminUserService adminUserService;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private PasswordUtil passwordUtil;
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		if (request.getCharacterEncoding() == null) {
		    request.setCharacterEncoding("UTF-8");
		}
		
		String context = request.getServletContext().getContextPath();
		String path = request.getRequestURI().substring(
				context.length());
		
		HttpSession httpSession = request.getSession(false);
		
		if (logger.isTraceEnabled())
			logger.trace("Prechain Session is: {}", httpSession);
		
		if (path.startsWith("/resources/") ||
			path.startsWith("/javax.faces.resource/") ||
			path.startsWith("/welcome/") ||
			path.startsWith("/Shibboleth.sso/") ||
			path.startsWith("/saml/") ||
			path.equals("/favicon.ico")
				) {
			chain.doFilter(servletRequest, servletResponse);
		}
		else if (path.startsWith("/admin") 
				&& (httpSession == null || (! session.isLoggedIn()))) {
			processAdminLogin(path, request, response, chain);
		}
		else if (path.startsWith("/rest/direct-auth") 
				&& (httpSession == null || (! session.isLoggedIn()))) {
			processDirectAuth(path, request, response, chain);
		}
		else if (path.startsWith("/rest") 
				&& (httpSession == null || (! session.isLoggedIn()))) {
			processRestLogin(path, request, response, chain);
		}
		else if (path.startsWith("/register/") && session != null && session.getUserId() == null) {
			chain.doFilter(servletRequest, servletResponse);
		}
		else if (session != null && session.isLoggedIn()) {

			Set<RoleEntity> roles = new HashSet<RoleEntity>(roleService.findByUserId(session.getUserId()));
			session.addRoles(roles);
			
			if (accessChecker.check(path, roles)) {
    			request.setAttribute(USER_ID, session.getUserId());
				chain.doFilter(servletRequest, servletResponse);
			}
			else
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed");
		}
		else {
			logger.debug("User from {} not logged in. Redirecting to welcome page", request.getRemoteAddr());
			
			session.setOriginalIdpEntityId(request.getParameter("idp"));
			session.setOriginalRequestPath(getFullURL(request));
			request.getServletContext().getRequestDispatcher("/welcome/").forward(servletRequest, servletResponse);
		}
		
		if (logger.isTraceEnabled()) {
			httpSession = request.getSession(false);
			logger.trace("Postchain Session is: {}", httpSession);
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		if (Security.getProvider("BC") == null) {
			logger.info("Register bouncy castle crypto provider");
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		}
	}

	private void processAdminLogin(String path, HttpServletRequest request, 
			HttpServletResponse response, FilterChain chain) 
		throws IOException, ServletException {

		processHttpLogin(path, request, response, chain, true);
	}

	private void processRestLogin(String path, HttpServletRequest request, 
			HttpServletResponse response, FilterChain chain) 
		throws IOException, ServletException {

		processHttpLogin(path, request, response, chain, false);
	}

	private void processHttpLogin(String path, HttpServletRequest request, 
			HttpServletResponse response, FilterChain chain, boolean setRoles) 
		throws IOException, ServletException {

	    String auth = request.getHeader("Authorization");
	    if (auth != null) {
	    	int index = auth.indexOf(' ');
	        if (index > 0) {
	        	String[] credentials = StringUtils.split(
	        			new String(Base64.decodeBase64(auth.substring(index).getBytes())), ":", 2);
	
	        	if (credentials.length == 2) {
	        		AdminUserEntity adminUser = adminUserService.findByUsername(
	        				credentials[0]);
	        		
	        		if (adminUser != null && passwordsMatch(adminUser.getPassword(), credentials[1])) {
	        			
						List<RoleEntity> roleList = adminUserService.findRolesForUserById(adminUser.getId());
	        			Set<RoleEntity> roles = new HashSet<RoleEntity>(roleList);
	        			
	        			if (setRoles && session != null)
	        				session.addRoles(roles);
	        			
		        		if (accessChecker.check(path, roles)) {
		        			request.setAttribute(ADMIN_USER_ID, adminUser.getId());
			        		chain.doFilter(request, response);
		        		}
	        			else
	        				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed");
	        			
		        		return;
	        		}
	        	}
	        }
	    }
		
		response.setHeader( "WWW-Authenticate", "Basic realm=\"Admin Realm\"" );
		response.sendError( HttpServletResponse.SC_UNAUTHORIZED );		
	}

	private void processDirectAuth(String path, HttpServletRequest request, 
			HttpServletResponse response, FilterChain chain) 
		throws IOException, ServletException {

		if (appConfig.getConfigValue("direct_auth_allow") == null) {
			logger.info("Denying direct-auth from {}", request.getRemoteAddr());
			response.sendError( HttpServletResponse.SC_NOT_ACCEPTABLE );
			return;
		}

		String directAuthAllow = appConfig.getConfigValue("direct_auth_allow");
		if (! request.getRemoteAddr().matches(directAuthAllow)) {
			logger.info("Denying direct-auth from {}. Does not match.", request.getRemoteAddr());
			response.sendError( HttpServletResponse.SC_NOT_ACCEPTABLE );
			return;
		}
		
	    String auth = request.getHeader("Authorization");
	    if (auth != null) {
	    	int index = auth.indexOf(' ');
	        if (index > 0) {
	        	String[] credentials = StringUtils.split(
	        			new String(Base64.decodeBase64(auth.substring(index).getBytes())), ":", 2);
	
	        	if (credentials.length == 2) {
	        		request.setAttribute(DIRECT_USER_ID, credentials[0]);
	        		request.setAttribute(DIRECT_USER_PW, credentials[1]);
	        		chain.doFilter(request, response);
	        		return;
	        	}
	        }
	    }
		
		response.setHeader( "WWW-Authenticate", "Basic realm=\"Admin Realm\"" );
		response.sendError( HttpServletResponse.SC_UNAUTHORIZED );		
	}

	private boolean passwordsMatch(String password, String comparePassword) {
		if (password == null || comparePassword == null)
			return false;
		if (password.matches("^\\{(.+)\\|(.+)\\|(.+)\\}$")) {
			return passwordUtil.comparePassword(comparePassword, password);
		}
		else {
			return comparePassword.equals(password);
		}
	}
	
	private String getFullURL(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder(request.getRequestURI());
		String query = request.getQueryString();
		
		if (query != null) {
			sb.append("?");
			sb.append(query);
		}
		
		return sb.toString();
	}
}
