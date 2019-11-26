package edu.kit.scc.webreg.oauth;

import java.io.IOException;

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

@Path("/realms")
public class OidcUserinfoController {

	@Inject
	private Logger logger;
	
	@GET
	@Path("/{realm}/protocol/openid-connect/userinfo")
	@Produces(MediaType.APPLICATION_JSON)
	public void userinfo(@PathParam("realm") String realm,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws IOException {
		
		logger.debug("userinfo called for {}", realm);

	}
}
