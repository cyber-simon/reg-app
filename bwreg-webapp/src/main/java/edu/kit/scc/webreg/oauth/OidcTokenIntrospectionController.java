package edu.kit.scc.webreg.oauth;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

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
	@Path("/{realm}/protocol/openid-connect/tokeninfo")
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
