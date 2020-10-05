package edu.kit.scc.webreg.service.oidc.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.nimbusds.jwt.JWT;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;

import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpFlowStateDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpFlowStateEntity;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

@Stateless
public class OidcClientCallbackServiceImpl implements OidcClientCallbackService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private OidcRpConfigurationDao rpConfigDao;
	
	@Inject
	private OidcRpFlowStateDao rpFlowStateDao;
	
	@Override
	public void callback(String uri) throws OidcAuthenticationException {

		try {
			AuthorizationResponse response = AuthorizationResponse.parse(new URI(uri));
		
			if (! response.indicatesSuccess()) {
				throw new OidcAuthenticationException("No success indicated with uri parsing");
			}
	
			AuthorizationSuccessResponse successResponse = (AuthorizationSuccessResponse) response;
	
			OidcRpFlowStateEntity flowState = rpFlowStateDao.findByState(successResponse.getState().getValue());
			
			// The returned state parameter must match the one send with the request
			if (flowState == null) {
				throw new OidcAuthenticationException("State is wrong or expired");
			}

			OidcRpConfigurationEntity rpConfig = flowState.getRpConfiguration();

			AuthorizationCode code = successResponse.getAuthorizationCode();
			flowState.setCode(code.getValue());

			URI callback = new URI("https://bwidm.scc.kit.edu/rpoidc/callback");
			AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);

			ClientID clientID = new ClientID(rpConfig.getClientId());
			Secret clientSecret = new Secret(rpConfig.getSecret());
			ClientAuthentication clientAuth = new ClientSecretBasic(clientID, clientSecret);

			// The token endpoint
			URI tokenEndpoint = new URI(rpConfig.getTokenEndpoint());

			// Make the token request
			TokenRequest tokenRequest = new TokenRequest(tokenEndpoint, clientAuth, codeGrant);
			TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

			if (! response.indicatesSuccess()) {
			    throw new OidcAuthenticationException("got token error response: " + response.toErrorResponse());
			}

			OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();
			
			JWT idToken = oidcTokenResponse.getOIDCTokens().getIDToken();
			AccessToken accessToken = oidcTokenResponse.getOIDCTokens().getAccessToken();
			RefreshToken refreshToken = oidcTokenResponse.getOIDCTokens().getRefreshToken();
			
		} catch (IOException | ParseException | URISyntaxException e) {
			logger.warn("Oidc callback failed: {}", e.getMessage());
			throw new OidcAuthenticationException(e);
		}
	}
}
