package edu.kit.scc.webreg.oauth;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

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
			@QueryParam("code_challenge") String codeChallange, @QueryParam("code_challenge_method") String codeChallangeMethod, 
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws IOException, OidcAuthenticationException {
		
		String red = opLogin.registerAuthRequest(realm, responseType, redirectUri, scope, state, nonce, clientId, 
				codeChallange, codeChallangeMethod, request, response);
		
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
