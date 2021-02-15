package edu.kit.scc.webreg.oauth;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

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
