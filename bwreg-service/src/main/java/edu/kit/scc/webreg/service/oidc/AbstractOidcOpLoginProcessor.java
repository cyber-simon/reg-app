package edu.kit.scc.webreg.service.oidc;

import java.io.IOException;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

public abstract class AbstractOidcOpLoginProcessor implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private CryptoHelper cryptoHelper;

	public abstract boolean matches(OidcClientConfigurationEntity clientConfig);

	public abstract String registerAuthRequest(OidcFlowStateEntity flowState, IdentityEntity identity)
			throws OidcAuthenticationException;

	public abstract JSONObject buildAccessToken(OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConfigurationEntity clientConfig, HttpServletResponse response) throws OidcAuthenticationException;

	public abstract JSONObject buildUserInfo(OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConfigurationEntity clientConfig, HttpServletResponse response) throws OidcAuthenticationException;

	public JSONObject sendError(ErrorObject error, HttpServletResponse response) {
		return sendError(error, response, null);
	}

	public JSONObject sendError(ErrorObject error, HttpServletResponse response, String errorDescription) {
		response.setStatus(error.getHTTPStatusCode());
		if (errorDescription != null) {
			error = error.setDescription(errorDescription);
		}
		return error.toJSONObject();
	}

	protected JWTClaimsSet.Builder initClaimsBuilder(OidcFlowStateEntity flowState) {
		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
		claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + (60L * 60L * 1000L)))
				.issuer("https://" + flowState.getOpConfiguration().getHost() + "/oidc/realms/"
						+ flowState.getOpConfiguration().getRealm())
				.claim("nonce", flowState.getNonce()).audience(flowState.getClientConfiguration().getName())
				.issueTime(new Date());
		return claimsBuilder;
	}

	protected SignedJWT signClaims(OidcOpConfigurationEntity opConfig, OidcClientConfigurationEntity clientConfig,
			JWTClaimsSet claims) throws OidcAuthenticationException {
		SignedJWT jwt;

		try {
			// MACSigner macSigner = new MACSigner(clientConfig.getSecret());

			PrivateKey privateKey = cryptoHelper.getPrivateKey(opConfig.getPrivateKey());
			X509Certificate certificate = cryptoHelper.getCertificate(opConfig.getCertificate());

			JWK jwk = JWK.parse(certificate);
			JWSHeader header;
			RSASSASigner rsaSigner = new RSASSASigner(privateKey);
			if (clientConfig.getGenericStore().containsKey("short_id_token_header")
					&& clientConfig.getGenericStore().get("short_id_token_header").equalsIgnoreCase("true")) {
				header = new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).keyID(jwk.getKeyID())
						.build();
			} else {
				header = new JWSHeader.Builder(JWSAlgorithm.RS256).jwk(jwk).type(JOSEObjectType.JWT)
						.keyID(jwk.getKeyID()).build();
			}
			jwt = new SignedJWT(header, claims);
			jwt.sign(rsaSigner);

		} catch (JOSEException | IOException e) {
			throw new OidcAuthenticationException(e);
		}

		return jwt;
	}

	protected OIDCTokenResponse finalizeTokenRespone(OidcFlowStateEntity flowState, SignedJWT jwt) {
		OidcOpConfigurationEntity opConfig = flowState.getOpConfiguration();
		OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();

		long accessTokenLifetime = 3600;
		if (clientConfig.getGenericStore().containsKey("access_token_lifetime")) {
			accessTokenLifetime = Long.parseLong(clientConfig.getGenericStore().get("access_token_lifetime"));
		}

		long refreshTokenLifetime = 7200;
		if (clientConfig.getGenericStore().containsKey("refresh_token_lifetime")) {
			refreshTokenLifetime = Long.parseLong(clientConfig.getGenericStore().get("refresh_token_lifetime"));
		}

		BearerAccessToken bat;
		if (clientConfig.getGenericStore().containsKey("long_access_token")
				&& clientConfig.getGenericStore().get("long_access_token").equalsIgnoreCase("true")) {
			bat = new BearerAccessToken(jwt.serialize(), accessTokenLifetime, new Scope(opConfig.getHost()));
		} else {
			bat = new BearerAccessToken(accessTokenLifetime, new Scope(opConfig.getHost()));
		}

		RefreshToken refreshToken = new RefreshToken();
		OIDCTokens tokens = new OIDCTokens(jwt, bat, refreshToken);
		OIDCTokenResponse tokenResponse = new OIDCTokenResponse(tokens);

		flowState.setAccessToken(bat.getValue());
		flowState.setAccessTokenType("Bearer");

		if (flowState.getRefreshToken() == null) {
			flowState.setRefreshToken(refreshToken.getValue());
			flowState.setValidUntil(new Date(System.currentTimeMillis() + (refreshTokenLifetime * 1000L)));
		} else if (flowState.getRefreshToken() != null
				&& clientConfig.getGenericStore().containsKey("refresh_token_extend")
				&& clientConfig.getGenericStore().get("refresh_token_extend").equalsIgnoreCase("true")) {
			flowState.setRefreshToken(refreshToken.getValue());
			flowState.setValidUntil(new Date(System.currentTimeMillis() + (refreshTokenLifetime * 1000L)));
		} else {
			flowState.setRefreshToken(refreshToken.getValue());
		}
		
		return tokenResponse;
	}
	
	protected ErrorObject verifyConfig(OidcOpConfigurationEntity opConfig, OidcClientConfigurationEntity clientConfig) {
		if (opConfig == null) {
			return OAuth2Error.REQUEST_NOT_SUPPORTED;
		} else if (clientConfig == null) {
			return OAuth2Error.INVALID_CLIENT;
		} else {
			return null;
		}
	}
}