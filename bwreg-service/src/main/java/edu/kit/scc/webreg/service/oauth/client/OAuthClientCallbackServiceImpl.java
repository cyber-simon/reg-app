package edu.kit.scc.webreg.service.oauth.client;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.dao.jpa.oauth.OAuthUserDao;
import edu.kit.scc.webreg.dao.jpa.oauth.RpOAuthFlowStateDao;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.entity.oauth.OAuthRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthRpFlowStateEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthRpFlowStateEntity_;
import edu.kit.scc.webreg.entity.oauth.OAuthUserEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthUserEntity_;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import edu.kit.scc.webreg.session.SessionManager;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class OAuthClientCallbackServiceImpl implements OAuthClientCallbackService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private RpOAuthFlowStateDao rpFlowStateDao;

	@Inject
	private OAuthUserDao oauthUserDao;

	@Inject
	private OAuthUserUpdater userUpdater;

	@Inject
	private UserLoginInfoDao userLoginInfoDao;

	@Inject
	private SessionManager session;

	private void callbackGithub(OAuthRpConfigurationEntity rpConfig, String callbackUrl, ClientID clientID,
			Secret clientSecret, int connectTimeout, int readTimeout, AuthorizationGrant codeGrant,
			HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws IOException, ParseException, OidcAuthenticationException {
		ClientAuthentication clientAuth = new ClientSecretPost(clientID, clientSecret);
		TokenRequest tokenRequest = new TokenRequest(URI.create("https://github.com/login/oauth/access_token"),
				clientAuth, codeGrant);
		HTTPRequest httpRequest = tokenRequest.toHTTPRequest();
		httpRequest.setConnectTimeout(connectTimeout);
		httpRequest.setReadTimeout(readTimeout);
		httpRequest.setHeader("Accept", "application/json");
		HTTPResponse r = httpRequest.send();

		ObjectMapper om = new ObjectMapper();

		@SuppressWarnings("unchecked")
		Map<String, Object> mapToken = om.readValue(r.getBody(), Map.class);
		logger.debug("Map: {}", mapToken.toString());
		String accessToken = mapToken.get("access_token").toString();

		RequestConfig config = RequestConfig.custom()
				.setConnectionRequestTimeout(Timeout.ofMilliseconds(connectTimeout)).build();
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(config).build();
		HttpGet httpget = new HttpGet("https://api.github.com/user");
		httpget.setHeader("Authorization", "Bearer " + accessToken);
		BasicHttpClientResponseHandler respnoseHandler = new BasicHttpClientResponseHandler();
		String response;
		try {
			response = httpclient.execute(httpget, respnoseHandler);
		} catch (ClientProtocolException e) {
			logger.info("Problem", e);
			return;
		} catch (IOException e) {
			logger.info("Problem", e);
			return;
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> mapUser = om.readValue(response, Map.class);
		Map<String, List<Object>> attributeMap = new HashMap<>();
		List<Object> userList = new ArrayList<>();
		userList.add(mapUser);
		attributeMap.put("user", userList);
		logger.debug("github user: " + mapUser);

		String userId = mapUser.get("id").toString();
		OAuthUserEntity user = findOAuthUserByIssuerAndSubject(rpConfig, userId);

		if (user != null) {
			MDC.put("userId", "" + user.getId());
		}

		/**
		 * TODO check more states here! Check session.getIdentityId. If it is set, user
		 * is already logged in. This should only happen with account linking There are
		 * two possiblities for account linking: user is null and user is not null. Not
		 * null means, user already exists.
		 */
		if (session.getIdentityId() != null) {
			/*
			 * we are in account linking mode. Session with identity is established
			 */
			if (user != null) {
				throw new OidcAuthenticationException("Linking two existing accounts is not supported yet");
			} else {
				logger.info("New User for account linking to identity {} detected, sending to register Page",
						session.getIdentityId());

				// Store OIDC Data temporarily in Session
				logger.debug("Storing relevant Oidc data in session");
				session.setSubjectId(userId);
				session.setAttributeMap(attributeMap);

				httpServletResponse.sendRedirect("/user/connect-account-oidc.xhtml");
				return;
			}
		}

		if (user == null) {
			logger.info("New User detected, sending to register Page");

			// Store OIDC Data temporarily in Session
			logger.debug("Storing relevant Oidc data in session");
			session.setSubjectId(userId);
			session.setAttributeMap(attributeMap);

			httpServletResponse.sendRedirect("/register/register-oauth.xhtml");
			return;
		}

		logger.debug("Updating OAuth user {}", user.getOauthId());

		try {
			user = userUpdater.updateUser(user, attributeMap, "web-sso", null, httpServletRequest.getServerName());
		} catch (UserUpdateException e) {
			logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
			throw new OidcAuthenticationException(e.getMessage());
		}

		session.setIdentityId(user.getIdentity().getId());
		session.setLoginTime(Instant.now());
		session.setTheme(user.getTheme());
		session.getLoggedInUserList().add(user.getId());

		UserLoginInfoEntity loginInfo = userLoginInfoDao.createNew();
		loginInfo.setUser(user);
		loginInfo.setLoginDate(new Date());
		loginInfo.setLoginMethod(UserLoginMethod.HOME_ORG);
		loginInfo.setLoginStatus(UserLoginInfoStatus.SUCCESS);
		loginInfo.setFrom(httpServletRequest.getRemoteAddr());
		loginInfo = userLoginInfoDao.persist(loginInfo);

		if (session.getOriginalRequestPath() != null) {
			String orig = session.getOriginalRequestPath();
			session.setOriginalRequestPath(null);
			httpServletResponse.sendRedirect(orig);
		} else
			httpServletResponse.sendRedirect("/index.xhtml");

		return;
	}

	@Override
	@RetryTransaction
	public void callback(String uri, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
			throws OidcAuthenticationException {

		try {
			AuthorizationResponse response = AuthorizationResponse.parse(new URI(uri));

			if (!response.indicatesSuccess()) {
				throw new OidcAuthenticationException("No success indicated with uri parsing");
			}

			AuthorizationSuccessResponse successResponse = (AuthorizationSuccessResponse) response;

			OAuthRpFlowStateEntity flowState = findOAuthRpFlowStateByState(successResponse.getState().getValue());

			// The returned state parameter must match the one send with the request
			if (flowState == null) {
				throw new OidcAuthenticationException("State is wrong or expired");
			}

			OAuthRpConfigurationEntity rpConfig = flowState.getRpConfiguration();

			int connectTimeout = 10 * 1000;
			int readTimeout = 10 * 1000;

			if (rpConfig.getGenericStore().containsKey("connect_timeout")) {
				connectTimeout = Integer.parseInt(rpConfig.getGenericStore().get("connect_timeout"));
			}

			if (rpConfig.getGenericStore().containsKey("read_timeout")) {
				readTimeout = Integer.parseInt(rpConfig.getGenericStore().get("read_timeout"));
			}

			AuthorizationCode code = successResponse.getAuthorizationCode();
			flowState.setCode(code.getValue());

			String callbackUrl;
			if (!rpConfig.getCallbackUrl().startsWith("https://")) {
				/*
				 * we are dealing with a relative acs endpoint. We have to build it with the
				 * called hostname;
				 */
				callbackUrl = "https://" + httpServletRequest.getServerName() + rpConfig.getCallbackUrl();
			} else {
				callbackUrl = rpConfig.getCallbackUrl();
			}

			URI callback = new URI(callbackUrl);
			AuthorizationGrant codeGrant = new AuthorizationCodeGrant(code, callback);

			ClientID clientID = new ClientID(rpConfig.getClientId());
			Secret clientSecret = new Secret(rpConfig.getSecret());

			callbackGithub(rpConfig, callbackUrl, clientID, clientSecret, readTimeout, connectTimeout, codeGrant,
					httpServletRequest, httpServletResponse);

		} catch (IOException | ParseException | URISyntaxException e) {
			logger.warn("Oidc callback failed: {}", e.getMessage());
			return;
			// throw new OidcAuthenticationException(e);
		}
	}

	private OAuthUserEntity findOAuthUserByIssuerAndSubject(OAuthRpConfigurationEntity issuer, String subjectId) {
		return oauthUserDao
				.find(and(equal(OAuthUserEntity_.oauthIssuer, issuer), equal(OAuthUserEntity_.oauthId, subjectId)));
	}

	private OAuthRpFlowStateEntity findOAuthRpFlowStateByState(String state) {
		return rpFlowStateDao.find(equal(OAuthRpFlowStateEntity_.state, state));
	}

}
