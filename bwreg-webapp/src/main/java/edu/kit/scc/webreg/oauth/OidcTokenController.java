package edu.kit.scc.webreg.oauth;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

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
