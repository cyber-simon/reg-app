package edu.kit.scc.webreg.oauth;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import edu.kit.scc.webreg.service.oidc.OidcOpLogin;
import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcTokenController {

	@Inject
	private Logger logger;
	
	@Inject
	private OidcOpLogin opLogin;
	
	@POST
	@Path("/{realm}/protocol/openid-connect/token")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject auth(@PathParam("realm") String realm, @FormParam("grant_type") String grantType,
			@FormParam("code") String code, @FormParam("redirect_uri") String redirectUri,
			@FormParam("client_id") String clientId, @FormParam("client_secret") String clientSecret,
			@FormParam("code_verifier") String codeVerifier, @FormParam("refresh_token") String refreshToken,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {

		logger.debug("Post token called for {} with code {} and grant_type {}", realm, code, grantType);

		if (clientId != null && (clientSecret != null || codeVerifier != null)) {
    		return opLogin.serveToken(realm, grantType, code, redirectUri, request, response, clientId, clientSecret, codeVerifier, refreshToken);			
		}
		
	    String auth = request.getHeader("Authorization");

	    if (auth != null) {
	    	int index = auth.indexOf(' ');
	        if (index > 0) {
	        	String[] credentials = StringUtils.split(
	        			new String(Base64.decodeBase64(auth.substring(index).getBytes())), ":", 2);
	        	
	        	if (credentials.length == 2) {
	        		return opLogin.serveToken(realm, grantType, code, redirectUri, request, response, credentials[0], credentials[1], codeVerifier, refreshToken);
	        	}
	        }
	    }

		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed");
		return null;
	}
	
}
