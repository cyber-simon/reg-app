package edu.kit.scc.webreg.oauth;

import java.io.IOException;
import java.util.Date;

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

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import net.minidev.json.JSONObject;

@Named
@WebServlet(urlPatterns = {"/oauth/token"})
public class TokenHandlerServlet implements Servlet {

	@Inject
	private Logger logger;

	@Inject
	private SessionManager session;
	
	@Inject
	private UserService userService;

	@Override
	public void init(ServletConfig config) throws ServletException {
		
	}

	@Override
	public void service(ServletRequest servletRequest, ServletResponse servletResponse)
			throws ServletException, IOException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		
		if (session == null || (! session.isLoggedIn())) {
			logger.debug("Client session from {} not logged in. Sending client back to welcome page",
					request.getRemoteAddr());
			response.sendRedirect("/welcome/index.xhtml");
			return;
		}

		UserEntity user = userService.findById(session.getUserId());
		
		logger.debug("User {} ({}) loaded", user.getId(), user.getEppn());
		JWTClaimsSet claims =  new JWTClaimsSet.Builder()
				      .subject("" + session.getUserId())
				      .expirationTime(new Date(System.currentTimeMillis() + (60L * 60L * 1000L)))
				      .claim("http://bwidm.scc.kit.edu/is_shibboleth", true)
				      .build();
		
		JWT jwt = new PlainJWT(claims);
		BearerAccessToken bat = new BearerAccessToken(3600, new Scope("bwidm.scc.kit.edu"));
		OIDCTokens tokens = new OIDCTokens(jwt, bat, null);
		OIDCTokenResponse tokenResponse = new OIDCTokenResponse(tokens);
		JSONObject jsonObject = tokenResponse.toJSONObject();
		jsonObject.writeJSONString(response.getWriter());
	}

	@Override
	public void destroy() {
	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public String getServletInfo() {
		return null;
	}	
}
