package edu.kit.scc.webreg.oauth;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

@Path("/realms")
public class OidcCertsController {

	@Inject
	private Logger logger;
	
	@GET
	@Path("/{realm}/protocol/openid-connect/certs")
	@Produces(MediaType.APPLICATION_JSON)
	public void auth(@PathParam("realm") String realm)
			throws IOException {
		
		logger.debug("certs called for {}", realm);
	}
}
