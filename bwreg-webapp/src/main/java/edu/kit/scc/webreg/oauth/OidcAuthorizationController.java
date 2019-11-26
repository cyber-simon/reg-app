package edu.kit.scc.webreg.oauth;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;

@Path("/realms")
public class OidcAuthorizationController {

	@Inject
	private Logger logger;
	
	@GET
	@Path("/{realm}/protocol/openid-connect/auth")
	public void auth(@PathParam("realm") String realm, @QueryParam("response_type") String responseType,
			@QueryParam("redirect_uri") String redirectUri, @QueryParam("scope") String scope,
			@QueryParam("state") String state, @QueryParam("nonce") String nonce, @QueryParam("client_id") String clientId,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws IOException {
		
		logger.debug("processing {} with redirect to {}", responseType, redirectUri);
		
		logger.debug("red: {}", request.getParameter("redirect_uri"));
		for (Entry<String, String[]> e : request.getParameterMap().entrySet()) {
			for (String s : e.getValue())
				logger.debug("param: {} value: {}", e.getKey(), s);
		}
		
		String red = redirectUri + "?code=" + UUID.randomUUID().toString() + "&state=" + state;
		logger.debug("Sending client to {}", red);
		response.sendRedirect(red);
	}
}
