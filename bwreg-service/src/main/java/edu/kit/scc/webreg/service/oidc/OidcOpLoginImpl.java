package edu.kit.scc.webreg.service.oidc;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

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
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.dao.oidc.OidcClientConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.OidcFlowStateDao;
import edu.kit.scc.webreg.dao.oidc.OidcOpConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.ServiceOidcClientDao;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity_;
import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.ServiceOidcClientEntity;
import edu.kit.scc.webreg.script.ScriptingEnv;
import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;
import net.minidev.json.JSONObject;

@Stateless
public class OidcOpLoginImpl implements OidcOpLogin {

	@Inject
	private Logger logger;

	@Inject
	private OidcFlowStateDao flowStateDao;

	@Inject
	private OidcOpConfigurationDao opDao;

	@Inject
	private OidcClientConfigurationDao clientDao;

	@Inject
	private ServiceOidcClientDao serviceOidcClientDao;

	@Inject
	private IdentityDao identityDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private SessionManager session;

	@Inject
	private CryptoHelper cryptoHelper;

	@Inject
	private ScriptingEnv scriptingEnv;

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionSingleton;

	@Inject
	private ApplicationConfig appConfig;

	@Override
	public String registerAuthRequest(String realm, String responseType, String redirectUri, String scope, String state,
			String nonce, String clientId, String codeChallange, String codeChallangeMethod, HttpServletRequest request,
			HttpServletResponse response) throws IOException, OidcAuthenticationException {

		checkCodeChallange(codeChallange, codeChallangeMethod);

		OidcOpConfigurationEntity opConfig = opDao.findByRealmAndHost(realm, request.getServerName());

		if (opConfig == null) {
			throw new OidcAuthenticationException(
					"unknown realm/host combination: " + realm + " / " + request.getServerName());
		}

		IdentityEntity identity = null;
		if (session.getIdentityId() != null) {
			identity = identityDao.fetch(session.getIdentityId());
		}

		OidcClientConfigurationEntity clientConfig = clientDao.findByNameAndOp(clientId, opConfig);

		if (clientConfig == null) {
			throw new OidcAuthenticationException("unknown client");
		}

		if (clientConfig.getGenericStore().containsKey("redirect_uri_regex")) {
			if (!redirectUri.matches(clientConfig.getGenericStore().get("redirect_uri_regex"))) {
				throw new OidcAuthenticationException("invalid redirect uri");
			}
		}

		OidcFlowStateEntity flowState = flowStateDao.createNew();
		flowState.setOpConfiguration(opConfig);
		flowState.setNonce(nonce);
		flowState.setState(state);
		flowState.setClientConfiguration(clientConfig);
		flowState.setResponseType(responseType);
		flowState.setCode(UUID.randomUUID().toString());
		flowState.setRedirectUri(redirectUri);
		flowState.setValidUntil(new Date(System.currentTimeMillis() + (30L * 60L * 1000L)));
		flowState.setCodeChallange(codeChallange);
		flowState.setCodecodeChallangeMethod(codeChallangeMethod);
		flowState.setScope(scope);
		flowState = flowStateDao.persist(flowState);
		session.setOidcFlowStateId(flowState.getId());
		session.setOidcAuthnOpConfigId(opConfig.getId());
		session.setOidcAuthnClientConfigId(clientConfig.getId());

		if (identity != null) {
			logger.debug("Client already logged in, sending to return page.");
			return "/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/auth/return";
		} else {
			logger.debug(
					"Client session from {} not established. In order to serve client must login. Sending to login page.",
					request.getRemoteAddr());

			session.setOriginalRequestPath(
					"/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/auth/return");
			return "/welcome/index.xhtml";
		}
	}

	@Override
	public String registerAuthRequestReturn(String realm, HttpServletRequest request, HttpServletResponse response)
			throws IOException, OidcAuthenticationException {

		OidcOpConfigurationEntity opConfig = opDao.findByRealmAndHost(realm, request.getServerName());

		if (opConfig == null) {
			throw new OidcAuthenticationException(
					"unknown realm/host combination: " + realm + " / " + request.getServerName());
		}

		IdentityEntity identity = null;
		if (session.getIdentityId() != null) {
			identity = identityDao.fetch(session.getIdentityId());
		}

		if (session.getOidcFlowStateId() != null) {
			if (identity == null) {
				throw new OidcAuthenticationException("User ID missing.");
			}

			OidcFlowStateEntity flowState = flowStateDao.fetch(session.getOidcFlowStateId());
			if (flowState == null) {
				throw new OidcAuthenticationException("Corresponding flow state not found.");
			}

			OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();
			List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);

			if (serviceOidcClientList.size() == 0) {
				throw new OidcAuthenticationException("no script is connected to client configuration");
			}

			Boolean wantsElevation = false;
			RegistryEntity registry = null;

			for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
				if (serviceOidcClient.getWantsElevation() != null && serviceOidcClient.getWantsElevation()) {
					wantsElevation = true;
				}

				ServiceEntity service = serviceOidcClient.getService();

				if (service != null) {
					logger.debug("Service for RP found: {}", service);

					registry = registryDao.findByServiceAndIdentityAndStatus(service, identity, RegistryStatus.ACTIVE);

					if (registry != null) {
						List<Object> objectList = checkRules(registry.getUser(), service, registry);
						List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
						List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

						if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
							return "/user/check-access.xhtml?regId=" + registry.getId();
						}
					} else {
						registry = registryDao.findByServiceAndIdentityAndStatus(service, identity,
								RegistryStatus.LOST_ACCESS);

						if (registry != null) {
							logger.info("Registration for user {} and service {} in state LOST_ACCESS, checking again",
									registry.getUser().getEppn(), service.getName());
							List<Object> objectList = checkRules(registry.getUser(), service, registry);
							List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
							List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

							if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
								logger.info(
										"Registration for user {} and service {} in state LOST_ACCESS stays, redirecting to check page",
										registry.getUser().getEppn(), service.getName());
								return "/user/check-access.xhtml?regId=" + registry.getId();
							}
						} else {
							logger.info(
									"No active registration for identity {} and service {}, redirecting to register page",
									identity.getId(), service.getName());
							session.setOriginalRequestPath(
									"/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/auth/return");
							return "/user/register-service.xhtml?serviceId=" + service.getId();
						}
					}
				}

				if (serviceOidcClient.getRulePackage() != null) {
					/*
					 * There is an access rule for this oidc client. Check it.
					 */

					List<Object> objectList = checkRules(identity, serviceOidcClient.getRulePackage());
					List<OverrideAccess> overrideAccessList = extractOverideAccess(objectList);
					List<UnauthorizedUser> unauthorizedUserList = extractUnauthorizedUser(objectList);

					if (overrideAccessList.size() == 0 && unauthorizedUserList.size() > 0) {
						return "/user/oidc-access-denied.xhtml?soidc=" + serviceOidcClient.getId();
					}
				}

				if (serviceOidcClient.getScript() != null) {
					List<String> unauthorizedList = knowledgeSessionSingleton
							.checkScriptAccess(serviceOidcClient.getScript(), identity);

					if (unauthorizedList.size() > 0) {
						return "/user/oidc-access-denied.xhtml?soidc=" + serviceOidcClient.getId();
					}
				}
			}

			if (wantsElevation) {
				long elevationTime = 5L * 60L * 1000L;
				if (appConfig.getConfigValue("elevation_time") != null) {
					elevationTime = Long.parseLong(appConfig.getConfigValue("elevation_time"));
				}

				if (session.getTwoFaElevation() == null
						|| (System.currentTimeMillis() - session.getTwoFaElevation().toEpochMilli()) > elevationTime) {
					// second factor is active for this service and web login
					// and user is not elevated yet
					session.setOriginalRequestPath(
							"/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/auth/return");
					return "/user/twofa-login.xhtml";
				}
			}

			flowState.setValidUntil(new Date(System.currentTimeMillis() + (10L * 60L * 1000L)));
			flowState.setIdentity(identity);
			flowState.setRegistry(registry);

			String red = flowState.getRedirectUri() + "?code=" + flowState.getCode() + "&state=" + flowState.getState();
			logger.debug("Sending client to {}", red);
			return red;
		}

		throw new OidcAuthenticationException("something went horribly wrong...");
	}

	@Override
	public JSONObject serveToken(String realm, HttpServletRequest request, HttpServletResponse response,
			String clientId, String clientSecret, String codeVerifier, MultivaluedMap<String, String> formParams)
			throws OidcAuthenticationException {

		String grantType = formParams.getFirst("grant_type");
		String code = formParams.getFirst("code");

		logger.debug("Post token called for {} with code {} and grant_type {}", realm, code, grantType);

		if (grantType == null) {
			return sendError(OAuth2Error.INVALID_REQUEST, response);
		}
		if (grantType.equals("authorization_code")) {
			return serveAuthorizationCode(realm, request, response, clientId, clientSecret, codeVerifier, formParams);
		} else if (grantType.equals("refresh_token")) {
			return serveRefreshToken(realm, request, response, clientId, clientSecret, formParams);
		} else if (grantType.equals("urn:ietf:params:oauth:grant-type:token-exchange")) {
			return serveTokenExchange(realm, request, response, clientId, clientSecret, formParams);
		} else {
			return sendError(OAuth2Error.UNSUPPORTED_GRANT_TYPE, response);
		}
	}

	@Override
	public JSONObject serveIntrospection(String realm, HttpServletRequest request, HttpServletResponse response,
			String authType, String authData, MultivaluedMap<String, String> formParams)
			throws OidcAuthenticationException {

		OidcOpConfigurationEntity opConfig = opDao.findByRealmAndHost(realm, request.getServerName());
		if (opConfig == null) {
			return sendError(OAuth2Error.ACCESS_DENIED, response,
					"unknown realm/host combination: " + realm + " / " + request.getServerName());
		}

		String token = formParams.getFirst("token");

		// Bearer token is sent with request. In this case, the token bearer calls the
		// introspection
		// endpoint for his own token. Token from form and bearer token must be the
		// same.
		OidcFlowStateEntity flowState = findOidcFlowStateByAccessToken(authType, authData);

		if (flowState != null && !flowState.getAccessToken().equals(token)) {
			return sendError(OAuth2Error.INVALID_CLIENT, response, "authentication failed");
		}

		// the second case is, that the enpoint is called with client_id and
		// client_secret
		// this is done via Basic Auth
		if (flowState == null && authType.equals("Basic")) {
			String[] credentials = StringUtils.split(new String(Base64.getDecoder().decode(authData.getBytes())), ":",
					2);

			if (credentials.length != 2) {
				return sendError(OAuth2Error.INVALID_CLIENT, response, "invalid client");
			}

			String clientId = credentials[0];
			String clientSecret = credentials[1];

			OidcClientConfigurationEntity clientConfig = clientDao.findByNameAndOp(clientId, opConfig);

			if (clientConfig == null) {
				return sendError(OAuth2Error.INVALID_CLIENT, response, "unknown client");
			}

			if (!clientConfig.getSecret().equals(clientSecret)) {
				return sendError(OAuth2Error.INVALID_CLIENT, response, "authentication failed");
			}

			flowState = flowStateDao.find(equal(OidcFlowStateEntity_.accessToken, token));
		}

		logger.debug("Token introspection called for {} with by {}", realm, authType);

		if (flowState == null) {
			TokenIntrospectionSuccessResponse.Builder builder = new TokenIntrospectionSuccessResponse.Builder(false);
			return builder.build().toJSONObject();
		}

		if (flowState.getValidUntil().before(new Date())) {
			TokenIntrospectionSuccessResponse.Builder builder = new TokenIntrospectionSuccessResponse.Builder(false);
			return builder.build().toJSONObject();
		}

		TokenIntrospectionSuccessResponse.Builder builder = new TokenIntrospectionSuccessResponse.Builder(true);
		if (flowState.getScope() != null)
			builder.scope(new Scope(flowState.getScope().split(" ")));
		builder.clientID(new ClientID(flowState.getClientConfiguration().getName()));
		builder.expirationTime(flowState.getValidUntil());
		return builder.build().toJSONObject();
	}

	@Override
	public JSONObject serveUserInfo(String realm, String tokeType, String tokenId, HttpServletRequest request,
			HttpServletResponse response) throws OidcAuthenticationException {

		OidcFlowStateEntity flowState = findOidcFlowStateByAccessToken(tokenId, tokeType);

		if (flowState == null) {
			throw new OidcAuthenticationException("No flow state found for token.");
		}

		OidcOpConfigurationEntity opConfig = flowState.getOpConfiguration();

		if (opConfig == null) {
			throw new OidcAuthenticationException("unknown realm");
		}

		OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();

		if (clientConfig == null) {
			throw new OidcAuthenticationException("unknown client");
		}

		List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);

		IdentityEntity identity = flowState.getIdentity();

		if (identity == null) {
			throw new OidcAuthenticationException("No identity attached to flow state.");
		}

		UserEntity user;
		if (identity.getUsers().size() == 1) {
			user = identity.getUsers().iterator().next();
		} else {
			user = identity.getPrefUser();
		}

		RegistryEntity registry = flowState.getRegistry();

		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();

		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			ScriptEntity scriptEntity = serviceOidcClient.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new OidcAuthenticationException(
							"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;

					invocable.invokeFunction("buildClaimsStatement", scriptingEnv, claimsBuilder, user, registry,
							serviceOidcClient.getService(), logger, identity);
				} catch (NoSuchMethodException | ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			} else {
				throw new OidcAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
		}
		UserInfo userInfo = new UserInfo(claimsBuilder.build());
		logger.debug("userInfo Response: " + userInfo.toJSONObject());
		return userInfo.toJSONObject();
	}

	private OidcFlowStateEntity findOidcFlowStateByAccessToken(String accessToken, String accessTokenType) {
		return flowStateDao.find(and(equal(OidcFlowStateEntity_.accessToken, accessToken),
				equal(OidcFlowStateEntity_.accessTokenType, accessTokenType)));
	}

	@Override
	public JSONObject serveUserJwt(String realm, HttpServletRequest request, HttpServletResponse response)
			throws OidcAuthenticationException {

		OidcOpConfigurationEntity opConfig = opDao.findByRealmAndHost(realm, request.getServerName());

		if (opConfig == null) {
			return sendError(OAuth2Error.ACCESS_DENIED, response,
					"unknown realm/host combination: " + realm + " / " + request.getServerName());
		}

		if (session.isLoggedIn()) {
			JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
			claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + (60L * 60L * 1000L)))
					.issuer("https://bwidm.scc.kit.edu/oidc/realms/intern").issueTime(new Date())
					.subject("" + session.getIdentityId()).build();

			JWTClaimsSet claims = claimsBuilder.build();

			logger.debug("claims before signing: " + claims.toJSONObject());

			SignedJWT jwt;
			try {
				// MACSigner macSigner = new MACSigner(clientConfig.getSecret());

				PrivateKey privateKey = cryptoHelper.getPrivateKey(opConfig.getPrivateKey());
				X509Certificate certificate = cryptoHelper.getCertificate(opConfig.getCertificate());
				JWK jwk = JWK.parse(certificate);
				RSASSASigner rsaSigner = new RSASSASigner(privateKey);
				JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256).jwk(jwk).keyID(jwk.getKeyID()).build();
				jwt = new SignedJWT(header, claims);
				jwt.sign(rsaSigner);
			} catch (JOSEException | IOException e) {
				throw new OidcAuthenticationException(e);
			}

			BearerAccessToken bat = new BearerAccessToken(3600, new Scope(opConfig.getHost()));
			OIDCTokens tokens = new OIDCTokens(jwt, bat, null);
			return tokens.toJSONObject();
		} else
			return sendError(OAuth2Error.INVALID_GRANT, response);
	}

	protected JSONObject serveTokenExchange(String realm, HttpServletRequest request, HttpServletResponse response,
			String clientId, String clientSecret, MultivaluedMap<String, String> formParams)
			throws OidcAuthenticationException {

		OidcOpConfigurationEntity opConfig = opDao.findByRealmAndHost(realm, request.getServerName());
		if (opConfig == null) {
			return sendError(OAuth2Error.ACCESS_DENIED, response,
					"unknown realm/host combination: " + realm + " / " + request.getServerName());
		}

		/*
		 * As required in https://datatracker.ietf.org/doc/rfc8693/
		 */
		String subjectToken = formParams.getFirst("subject_token");

		if (subjectToken == null) {
			return sendError(OAuth2Error.INVALID_REQUEST_OBJECT, response, "subject_token is required");
		}

		String subjectTokenType = formParams.getFirst("subject_token_type");
		if (subjectTokenType == null) {
			// subject_token_type is req'd, but if it is missing, let's guess it's an
			// access_token
			subjectTokenType = "urn:ietf:params:oauth:token-type:access_token";
		}

		OidcFlowStateEntity oldFlowState = null;

		if (subjectTokenType.equals("urn:ietf:params:oauth:token-type:access_token")) {
			oldFlowState = flowStateDao.find(equal(OidcFlowStateEntity_.accessToken, subjectToken));
		} else if (subjectTokenType.equals("urn:ietf:params:oauth:token-type:refresh_token")) {
			oldFlowState = flowStateDao.find(equal(OidcFlowStateEntity_.refreshToken, subjectToken));
		} else {
			return sendError(OAuth2Error.INVALID_REQUEST_OBJECT, response, "subject_token_type is not known");
		}

		if (oldFlowState == null) {
			return sendError(OAuth2Error.ACCESS_DENIED, response, "subject_token not found");
		}

		OidcClientConfigurationEntity oldClientConfig = oldFlowState.getClientConfiguration();
		OidcClientConfigurationEntity newClientConfig = clientDao.findByNameAndOp(clientId, opConfig);

		if (newClientConfig == null) {
			return sendError(OAuth2Error.ACCESS_DENIED, response, "Invalid client_id for realm");
		}

		if (newClientConfig.getSecret() != null && (!newClientConfig.getSecret().equals(clientSecret))) {
			return sendError(OAuth2Error.ACCESS_DENIED, response, "Invalid client_secret");
		}

		if ((!oldClientConfig.getGenericStore().containsKey("token_exchange_allow_to")) || (!newClientConfig.getName()
				.matches(oldClientConfig.getGenericStore().get("token_exchange_allow_to")))) {
			return sendError(OAuth2Error.ACCESS_DENIED, response, "Client is not allowed to exchange token");
		}

		OidcFlowStateEntity newFlowState = flowStateDao.createNew();
		newFlowState.setIdentity(oldFlowState.getIdentity());
		newFlowState.setOpConfiguration(opConfig);
		newFlowState.setClientConfiguration(newClientConfig);
		newFlowState.setResponseType("urn:ietf:params:oauth:grant-type:token-exchange");
		newFlowState.setScope("openid");
		newFlowState = flowStateDao.persist(newFlowState);

		return buildAccessToken(newFlowState, opConfig, newClientConfig);
	}

	protected JSONObject serveRefreshToken(String realm, HttpServletRequest request, HttpServletResponse response,
			String clientId, String clientSecret, MultivaluedMap<String, String> formParams)
			throws OidcAuthenticationException {

		String refreshToken = formParams.getFirst("refresh_token");
		OidcFlowStateEntity flowState = flowStateDao.find(equal(OidcFlowStateEntity_.refreshToken, refreshToken));

		if (flowState == null) {
			return sendError(OAuth2Error.INVALID_GRANT, response, "unknown refresh state");
		}

		OidcOpConfigurationEntity opConfig = flowState.getOpConfiguration();

		if (opConfig == null) {
			return sendError(OAuth2Error.ACCESS_DENIED, response, "unknown realm");
		}

		OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();

		if (clientConfig == null) {
			return sendError(OAuth2Error.INVALID_CLIENT, response);
		}

		if (!clientConfig.getName().equals(clientId)) {
			return sendError(OAuth2Error.INVALID_CLIENT, response);
		}

		if (clientSecret != null && (!clientConfig.getSecret().equals(clientSecret))) {
			// client_id and client_secret is set, but secret is wrong
			return sendError(OAuth2Error.INVALID_CLIENT, response);
		}

		return buildAccessToken(flowState, opConfig, clientConfig);
	}

	protected JSONObject serveAuthorizationCode(String realm, HttpServletRequest request, HttpServletResponse response,
			String clientId, String clientSecret, String codeVerifier, MultivaluedMap<String, String> formParams)
			throws OidcAuthenticationException {

		String code = formParams.getFirst("code");

		OidcFlowStateEntity flowState = flowStateDao.find(equal(OidcFlowStateEntity_.code, code));

		if (flowState == null) {
			return sendError(OAuth2Error.ACCESS_DENIED, response);
		}

		OidcOpConfigurationEntity opConfig = flowState.getOpConfiguration();
		OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();

		ErrorObject error = verifyConfig(opConfig, clientConfig);

		if (error != null) {
			return sendError(error, response);
		}

		if (!clientConfig.getName().equals(clientId)) {
			return sendError(OAuth2Error.INVALID_CLIENT, response);
		}

		if (clientSecret != null && (!clientConfig.getSecret().equals(clientSecret))) {
			// client_id and client_secret is set, but secret is wrong
			return sendError(OAuth2Error.INVALID_CLIENT, response);
		} else if (codeVerifier != null) {
			// check code verifier
			// code_verifier must be SHA256(flowState.getCodeChallange)

			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] encodedhash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
				String checkStr = new String(Base64.getUrlEncoder().withoutPadding().encode(encodedhash));
				if (!checkStr.equals(flowState.getCodeChallange())) {
					logger.debug("Code challange failed: {} <-> {}", checkStr, flowState.getCodeChallange());
					return sendError(OAuth2Error.VALIDATION_FAILED, response);
				}
			} catch (NoSuchAlgorithmException e) {
				throw new OidcAuthenticationException("cannot create hash at the moment. This is bad.");
			}
		}

		if (clientConfig.getGenericStore().containsKey("cors_allow_regex")) {
			String origin = request.getHeader("Origin");
			if (origin != null && origin.matches(clientConfig.getGenericStore().get("cors_allow_regex"))) {
				response.setHeader("Access-Control-Allow-Origin", origin);
			}
		}

		return buildAccessToken(flowState, opConfig, clientConfig);
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

	protected JSONObject sendError(ErrorObject error, HttpServletResponse response) {
		return sendError(error, response, null);
	}

	protected JSONObject sendError(ErrorObject error, HttpServletResponse response, String errorDescription) {
		response.setStatus(error.getHTTPStatusCode());
		if (errorDescription != null) {
			error = error.setDescription(errorDescription);
		}
		return error.toJSONObject();
	}

	protected JSONObject buildAccessToken(OidcFlowStateEntity flowState, OidcOpConfigurationEntity opConfig,
			OidcClientConfigurationEntity clientConfig) throws OidcAuthenticationException {

		IdentityEntity identity = flowState.getIdentity();

		if (identity == null) {
			throw new OidcAuthenticationException("No identity attached to flow state.");
		}

		UserEntity user;
		if (identity.getUsers().size() == 1) {
			user = identity.getUsers().iterator().next();
		} else {
			user = identity.getPrefUser();
		}

		RegistryEntity registry = flowState.getRegistry();

		/*
		 * allow for no registry
		 */
//		if (registry == null) {
//			throw new OidcAuthenticationException("No registry attached to flow state.");
//		}

		List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);

		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
		claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + (60L * 60L * 1000L)))
				.issuer("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm())
				.claim("nonce", flowState.getNonce()).audience(clientConfig.getName()).issueTime(new Date())
				.subject(user.getEppn());

		if (flowState.getScope() != null) {
			claimsBuilder.claim("scope", flowState.getScope());
		}

		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			ScriptEntity scriptEntity = serviceOidcClient.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new OidcAuthenticationException(
							"service not configured properly. engine not found: " + scriptEntity.getScriptEngine());

				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;

					invocable.invokeFunction("buildTokenStatement", scriptingEnv, claimsBuilder, user, registry,
							serviceOidcClient.getService(), logger, identity);
				} catch (NoSuchMethodException | ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			} else {
				throw new OidcAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
		}

		JWTClaimsSet claims = claimsBuilder.build();

		logger.debug("claims before signing: " + claims.toJSONObject());

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

		logger.debug("tokenResponse: " + tokenResponse.toJSONObject());

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

		return tokenResponse.toJSONObject();
	}

	private List<Object> checkRules(UserEntity user, ServiceEntity service, RegistryEntity registry) {
		return knowledgeSessionSingleton.checkServiceAccessRule(user, service, registry, "user-self", false);
	}

	private List<Object> checkRules(IdentityEntity identity, BusinessRulePackageEntity rulePackage) {
		return knowledgeSessionSingleton.checkIdentityRule(rulePackage, identity);
	}

	private List<OverrideAccess> extractOverideAccess(List<Object> objectList) {
		List<OverrideAccess> returnList = new ArrayList<OverrideAccess>();

		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				returnList.add((OverrideAccess) o);
			}
		}

		return returnList;
	}

	private List<UnauthorizedUser> extractUnauthorizedUser(List<Object> objectList) {
		List<UnauthorizedUser> returnList = new ArrayList<UnauthorizedUser>();

		for (Object o : objectList) {
			if (o instanceof UnauthorizedUser) {
				returnList.add((UnauthorizedUser) o);
			}
		}

		return returnList;
	}

	private void checkCodeChallange(String codeChallange, String codeChallangeMethod)
			throws OidcAuthenticationException {
		if (codeChallange != null) {
			if (codeChallange.length() > 511) {
				throw new OidcAuthenticationException("Code challange is not acceptable");
			}
		}
		if (codeChallangeMethod != null) {
			if (!CodeChallengeMethod.S256.toString().equals(codeChallangeMethod)) {
				throw new OidcAuthenticationException("Code challange method is not supported");
			}
		}
	}
}
