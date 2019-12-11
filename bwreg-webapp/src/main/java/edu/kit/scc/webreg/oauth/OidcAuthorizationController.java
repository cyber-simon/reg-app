package edu.kit.scc.webreg.oauth;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import edu.kit.scc.webreg.service.oidc.OidcOpLogin;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

@Path("/realms")
public class OidcAuthorizationController {

	@Inject
	private OidcOpLogin opLogin;
	
	@GET
	@Path("/{realm}/protocol/openid-connect/auth")
	public void auth(@PathParam("realm") String realm, @QueryParam("response_type") String responseType,
			@QueryParam("redirect_uri") String redirectUri, @QueryParam("scope") String scope,
			@QueryParam("state") String state, @QueryParam("nonce") String nonce, @QueryParam("client_id") String clientId,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws IOException, OidcAuthenticationException {
		
		String red = opLogin.registerAuthRequest(realm, responseType, redirectUri, scope, state, nonce, clientId, request, response);
		
		response.sendRedirect(red);
	}
	
	@GET
	@Path("/{realm}/protocol/openid-connect/auth/return")
	public void authReturn(@PathParam("realm") String realm, @Context HttpServletRequest request, @Context HttpServletResponse response)
			throws IOException, OidcAuthenticationException {
		
		String red = opLogin.registerAuthRequestReturn(realm, request, response);
		
		response.sendRedirect(red);
	}
	
}
