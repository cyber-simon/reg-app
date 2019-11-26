package edu.kit.scc.webreg.oauth;

import java.util.Date;

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

import org.slf4j.Logger;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcTokenController {

	@Inject
	private Logger logger;
	
	@POST
	@Path("/{realm}/protocol/openid-connect/token")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject auth(@PathParam("realm") String realm, @FormParam("grant_type") String grantType,
			@FormParam("code") String code, @FormParam("redirect_uri") String redirectUri,
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {

		logger.debug("Post token called for {} with code {} and grant_type {}", realm, code, grantType);

		JWTClaimsSet claims =  new JWTClaimsSet.Builder()
			      .subject("ls1947@kit.edu")
			      .expirationTime(new Date(System.currentTimeMillis() + (60L * 60L * 1000L)))
			      .claim("http://bwidm.scc.kit.edu/is_shibboleth", true)
			      .build();

		MACSigner macSigner = new MACSigner("qwertzuiopasdfghjklyxcvbnm12345678901234567890");

		SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
		jwt.sign(macSigner);
		
		BearerAccessToken bat = new BearerAccessToken(3600, new Scope("bwidm.scc.kit.edu"));
		OIDCTokens tokens = new OIDCTokens(jwt, bat, null);
		OIDCTokenResponse tokenResponse = new OIDCTokenResponse(tokens);
		
		logger.debug("tokenResponse: " + tokenResponse.toJSONObject());
		
		return tokenResponse.toJSONObject();
	}
	
}
