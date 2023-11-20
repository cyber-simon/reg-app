package edu.kit.scc.webreg.oauth;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import edu.kit.scc.webreg.service.oidc.OidcOpLogin;
import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcTokenController {

	@Inject
	private OidcOpLogin opLogin;
	
	@POST
	@Path("/{realm}/protocol/openid-connect/token")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject token(@PathParam("realm") String realm, MultivaluedMap<String,String> formParams, 
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {
		String clientId = formParams.getFirst("client_id");
		String clientSecret = formParams.getFirst("client_secret");
		String codeVerifier = formParams.getFirst("code_verifier");
		
		if (clientId != null && (clientSecret != null || codeVerifier != null)) {
    		return opLogin.serveToken(realm, request, response, clientId, clientSecret, codeVerifier, formParams);			
		}
		
	    String auth = request.getHeader("Authorization");

	    if (auth != null) {
	    	int index = auth.indexOf(' ');
	        if (index > 0) {
	        	String[] credentials = StringUtils.split(
	        			new String(Base64.decodeBase64(auth.substring(index).getBytes())), ":", 2);
	        	
	        	if (credentials.length == 2) {
	        		return opLogin.serveToken(realm, request, response, credentials[0], credentials[1], codeVerifier, formParams);
	        	}
	        }
	    }

		response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not allowed");
		return null;
	}
	
}
