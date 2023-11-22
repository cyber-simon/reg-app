package edu.kit.scc.webreg.oauth;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;

import edu.kit.scc.webreg.service.oidc.OidcOpLogin;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcUserinfoController {

	@Inject
	private Logger logger;
	
	@Inject
	private OidcOpLogin opLogin;
	
	@GET
	@Path("/{realm}/protocol/openid-connect/userinfo")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject userinfo(@PathParam("realm") String realm,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws OidcAuthenticationException {

		logger.debug("userinfo called for {}", realm);

		String authHeader = request.getHeader("Authorization");
		String[] authHeaders = authHeader.split(" ", 2);
		
		if (authHeaders.length == 2) {
			logger.debug("Authorization header: {} type: {}", authHeaders[1], authHeaders[0]);
			return opLogin.serveUserInfo(realm, authHeaders[0], authHeaders[1], request, response);
		}
		
		return null;
	}
}
