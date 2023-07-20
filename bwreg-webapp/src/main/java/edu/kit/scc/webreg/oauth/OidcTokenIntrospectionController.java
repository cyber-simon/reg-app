package edu.kit.scc.webreg.oauth;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;

import edu.kit.scc.webreg.service.oidc.OidcOpLogin;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcTokenIntrospectionController {

	@Inject
	private Logger logger;

	@Inject
	private OidcOpLogin opLogin;

	@POST
	@Path("/{realm}/protocol/openid-connect/token_info")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject userinfo(@PathParam("realm") String realm, MultivaluedMap<String, String> formParams,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws OidcAuthenticationException {

		logger.debug("token introspection called for {}", realm);

		String authHeader = request.getHeader("Authorization");
		String[] authHeaders = authHeader.split(" ", 2);

		if (authHeaders.length == 2) {
			logger.debug("Authorization header: {} type: {}", authHeaders[1], authHeaders[0]);
			return opLogin.serveIntrospection(realm, request, response, authHeaders[0], authHeaders[1], formParams);
		}

		return null;
	}
}
