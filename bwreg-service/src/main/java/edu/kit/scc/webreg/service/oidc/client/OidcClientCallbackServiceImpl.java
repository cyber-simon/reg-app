package edu.kit.scc.webreg.service.oidc.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.nimbusds.jwt.JWT;
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
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderConfigurationRequest;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

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
	
	@Inject
	private OidcOpMetadataSingletonBean opMetadataBean;

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

			// Make the token request
			TokenRequest tokenRequest = new TokenRequest(opMetadataBean.getTokenEndpointURI(rpConfig), 
					clientAuth, codeGrant);
			TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenRequest.toHTTPRequest().send());

			if (! tokenResponse.indicatesSuccess()) {
			    throw new OidcAuthenticationException("got token error response: " + tokenResponse.toErrorResponse().getErrorObject().getDescription());
			}

			OIDCTokenResponse oidcTokenResponse = (OIDCTokenResponse) tokenResponse.toSuccessResponse();

			JWT idToken = oidcTokenResponse.getOIDCTokens().getIDToken();
			AccessToken accessToken = oidcTokenResponse.getOIDCTokens().getAccessToken();
			RefreshToken refreshToken = oidcTokenResponse.getOIDCTokens().getRefreshToken();

			BearerAccessToken bat = oidcTokenResponse.getOIDCTokens().getBearerAccessToken();
			HTTPResponse httpResponse = new UserInfoRequest(
					opMetadataBean.getUserInfoEndpointURI(rpConfig), bat)
				    .toHTTPRequest()
				    .send();
			
			UserInfoResponse userInfoResponse = UserInfoResponse.parse(httpResponse);

			if (! userInfoResponse.indicatesSuccess()) {
			    throw new OidcAuthenticationException("got userinfo error response: " + 
			    		userInfoResponse.toErrorResponse().getErrorObject().getDescription());
			}
			
			UserInfo userInfo = userInfoResponse.toSuccessResponse().getUserInfo();
			logger.info("userinfo {}, {}, {}", userInfo.getSubject(), userInfo.getPreferredUsername(), 
					userInfo.getEmailAddress());
		} catch (IOException | ParseException | URISyntaxException e) {
			logger.warn("Oidc callback failed: {}", e.getMessage());
			throw new OidcAuthenticationException(e);
		}
	}
}
