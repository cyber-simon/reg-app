package edu.kit.scc.webreg.service.oidc;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MultivaluedMap;

import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import net.minidev.json.JSONObject;

public interface OidcOpLogin {

	String registerAuthRequest(String realm, String responseType, String redirectUri, String scope, String state,
			String nonce, String clientId,
			String codeChallange, String codeChallangeMethod, 
			HttpServletRequest request, HttpServletResponse response)
					 throws IOException, OidcAuthenticationException ;

	JSONObject serveUserInfo(String realm, String tokeType, String tokenId, HttpServletRequest request,
			HttpServletResponse response) throws OidcAuthenticationException;

	String registerAuthRequestReturn(String realm, HttpServletRequest request, HttpServletResponse response)
			throws IOException, OidcAuthenticationException;

	JSONObject serveUserJwt(String realm, HttpServletRequest request, HttpServletResponse response) throws OidcAuthenticationException;

	JSONObject serveToken(String realm, HttpServletRequest request, HttpServletResponse response, String clientId,
			String clientSecret, String codeVerifier, MultivaluedMap<String, String> formParams)
			throws OidcAuthenticationException;

	JSONObject serveIntrospection(String realm, HttpServletRequest request, HttpServletResponse response,
			String clientId, String clientSecret, MultivaluedMap<String, String> formParams)
			throws OidcAuthenticationException;

}
