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
import net.minidev.json.JSONObject;

@Path("/realms")
public class JwtAuthController {

	@Inject
	private Logger logger;
	
	@Inject
	private OidcOpLogin opLogin;
	
	@GET
	@Path("/{realm}/jwt")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject jwt(@PathParam("realm") String realm, @Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {

		logger.debug("Token JWT called");

		return opLogin.serveUserJwt(realm, request, response);
	}
	
}
