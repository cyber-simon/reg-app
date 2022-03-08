package edu.kit.scc.webreg.service.oidc;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

	JSONObject serveToken(String realm, String grantType, String code, String redirectUri, HttpServletRequest request,
			HttpServletResponse response, String clientId, String clientSecret, String codeVerifier, String refreshToken) throws OidcAuthenticationException;

}
