package edu.kit.scc.webreg.service.oidc;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import com.nimbusds.openid.connect.sdk.token.OIDCTokens;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.oidc.OidcClientConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.OidcFlowStateDao;
import edu.kit.scc.webreg.dao.oidc.OidcOpConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.ServiceOidcClientDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcClientConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcFlowStateEntity;
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
	private UserDao userDao;

	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private SessionManager session;
	
	@Inject
	private CryptoHelper cryptoHelper;
	
	@Inject
	private ScriptingEnv scriptingEnv;
	
	@Override
	public String registerAuthRequest(String realm, String responseType,
			String redirectUri, String scope,
			String state, String nonce, String clientId,
			HttpServletRequest request, HttpServletResponse response) throws IOException, OidcAuthenticationException {

		OidcOpConfigurationEntity opConfig = opDao.findByRealm(realm);
		
		if (opConfig == null) {
			throw new OidcAuthenticationException("unknown realm");
		}
		
		UserEntity user = null;
		if (session.getUserId() != null) {
			user = userDao.findById(session.getUserId());
		}
		
		OidcClientConfigurationEntity clientConfig = clientDao.findByNameAndOp(clientId, opConfig);

		if (clientConfig == null) {
			throw new OidcAuthenticationException("unknown client");
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
		flowState = flowStateDao.persist(flowState);

		if (user != null) {
			logger.debug("Client already logged in, sending to return page.");
			session.setAuthnRequestId(flowState.getId());
			return "/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/auth/return";		
		}
		else {
			logger.debug("Client session from {} not established. In order to serve client must login. Sending to login page.",
					request.getRemoteAddr());
			
			session.setAuthnRequestId(flowState.getId());
			session.setOriginalRequestPath("/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/auth/return");
			return "/welcome/index.xhtml";
		}
	}

	@Override
	public String registerAuthRequestReturn(String realm, HttpServletRequest request, HttpServletResponse response)
			throws IOException, OidcAuthenticationException {
		
		OidcOpConfigurationEntity opConfig = opDao.findByRealm(realm);
		
		if (opConfig == null) {
			throw new OidcAuthenticationException("unknown realm");
		}

		UserEntity user = null;
		if (session.getUserId() != null) {
			user = userDao.findById(session.getUserId());
		}
		
		if (session.getAuthnRequestId() != null) {
			if (user == null) {
				throw new OidcAuthenticationException("User ID missing.");
			}

			OidcFlowStateEntity flowState = flowStateDao.findById(session.getAuthnRequestId());
			if (flowState == null) {
				throw new OidcAuthenticationException("Corresponding flow state not found.");
			}

			OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();
			List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);
			
			if (serviceOidcClientList.size() == 0) {
				throw new OidcAuthenticationException("no service is connected to client configuration");
			}
			
			RegistryEntity registry = null;
			for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
				ServiceEntity service = serviceOidcClient.getService();
				logger.debug("Service for RP found: {}", service);
				registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
				if (registry == null) {
					logger.info("No active registration for user {} and service {}, redirecting to register page", 
							user.getEppn(), service.getName());
					return "/user/register-service.xhtml?serviceId=" + service.getId();
				}
			}
			
			flowState.setValidUntil(new Date(System.currentTimeMillis() + (10L * 60L * 1000L)));
			flowState.setUser(user);
			flowState.setRegistry(registry);
			
			String red = flowState.getRedirectUri() + "?code=" + flowState.getCode() + "&state=" + flowState.getState();
			logger.debug("Sending client to {}", red);
			return red;
		}

		throw new OidcAuthenticationException("something went horribly wrong...");
	}
	
	@Override
	public JSONObject serveToken(String realm, String grantType,
			String code, String redirectUri,
			HttpServletRequest request, HttpServletResponse response) throws OidcAuthenticationException {
		
		OidcFlowStateEntity flowState = flowStateDao.findByCode(code);

		if (flowState == null) {
			throw new OidcAuthenticationException("unknown flow state");
		}

		OidcOpConfigurationEntity opConfig = flowState.getOpConfiguration();
		
		if (opConfig == null) {
			throw new OidcAuthenticationException("unknown realm");
		}
		
		OidcClientConfigurationEntity clientConfig = flowState.getClientConfiguration();

		if (clientConfig == null) {
			throw new OidcAuthenticationException("unknown client");
		}
	
		UserEntity user = flowState.getUser();

		if (user == null) {
			throw new OidcAuthenticationException("No user attached to flow state.");
		}

		RegistryEntity registry = flowState.getRegistry();

		if (registry == null) {
			throw new OidcAuthenticationException("No registry attached to flow state.");
		}
		
		List<ServiceOidcClientEntity> serviceOidcClientList = serviceOidcClientDao.findByClientConfig(clientConfig);

		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
		claimsBuilder.expirationTime(new Date(System.currentTimeMillis() + (60L * 60L * 1000L)))
	      .issuer("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm())
	      .claim("nonce", flowState.getNonce())
	      .audience(flowState.getClientConfiguration().getName())
	      .issueTime(new Date())
	      .subject(flowState.getUser().getEppn())
	      .build();
		
		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			ScriptEntity scriptEntity = serviceOidcClient.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new OidcAuthenticationException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());
				
				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;
					
					invocable.invokeFunction("buildTokenStatement", scriptingEnv, claimsBuilder, user, registry, 
							serviceOidcClient.getService(), logger);
				} catch (NoSuchMethodException | ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			}
			else {
				throw new OidcAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
		}
		
		JWTClaimsSet claims =  claimsBuilder.build();

		logger.debug("claims before signing: " + claims.toJSONObject());
		
		SignedJWT jwt;
		try {
			//MACSigner macSigner = new MACSigner(clientConfig.getSecret());
			
			PrivateKey privateKey = cryptoHelper.getPrivateKey(opConfig.getPrivateKey());
			RSASSASigner rsaSigner = new RSASSASigner(privateKey);
			jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
			jwt.sign(rsaSigner);
		} catch (JOSEException | IOException e) {
			throw new OidcAuthenticationException(e);
		}
		
		BearerAccessToken bat = new BearerAccessToken(3600, new Scope(opConfig.getHost()));
		OIDCTokens tokens = new OIDCTokens(jwt, bat, null);
		OIDCTokenResponse tokenResponse = new OIDCTokenResponse(tokens);

		logger.debug("tokenResponse: " + tokenResponse.toJSONObject());
		
		flowState.setAccessToken(bat.getValue());
		flowState.setAccessTokenType("Bearer");
		flowState.setValidUntil(new Date(System.currentTimeMillis() + bat.getLifetime()));
		
		return tokenResponse.toJSONObject();
	}
	
	@Override
	public JSONObject serveUserInfo(String realm, String tokeType, String tokenId, 
			HttpServletRequest request, HttpServletResponse response) throws OidcAuthenticationException {
		
		OidcFlowStateEntity flowState = flowStateDao.findByAccessToken(tokenId, tokeType);

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
		UserEntity user = flowState.getUser();

		if (user == null) {
			throw new OidcAuthenticationException("No user attached to flow state.");
		}

		RegistryEntity registry = flowState.getRegistry();

		if (registry == null) {
			throw new OidcAuthenticationException("No registry attached to flow state.");
		}

		JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
		
		for (ServiceOidcClientEntity serviceOidcClient : serviceOidcClientList) {
			ScriptEntity scriptEntity = serviceOidcClient.getScript();
			if (scriptEntity.getScriptType().equalsIgnoreCase("javascript")) {
				ScriptEngine engine = (new ScriptEngineManager()).getEngineByName(scriptEntity.getScriptEngine());

				if (engine == null)
					throw new OidcAuthenticationException("service not configured properly. engine not found: " + scriptEntity.getScriptEngine());
				
				try {
					engine.eval(scriptEntity.getScript());

					Invocable invocable = (Invocable) engine;
					
					invocable.invokeFunction("buildClaimsStatement", scriptingEnv, claimsBuilder, user, registry, 
							serviceOidcClient.getService(), logger);
				} catch (NoSuchMethodException | ScriptException e) {
					logger.warn("Script execution failed. Continue with other scripts.", e);
				}
			}
			else {
				throw new OidcAuthenticationException("unkown script type: " + scriptEntity.getScriptType());
			}
		}
		UserInfo userInfo = new UserInfo(claimsBuilder.build());
		logger.debug("userInfo Response: " + userInfo.toJSONObject());
		return userInfo.toJSONObject();
	}	
}
