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
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

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
			@Context HttpServletRequest request, @Context HttpServletResponse response)
			throws Exception {

		logger.debug("Post token called for {} with code {} and grant_type {}", realm, code, grantType);

		return opLogin.serveToken(realm, grantType, code, redirectUri, request, response);
	}
	
}
