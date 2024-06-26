package edu.kit.scc.webreg.service.oauth.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.dao.jpa.oauth.OAuthRpConfigurationDao;
import edu.kit.scc.webreg.dao.jpa.oauth.RpOAuthFlowStateDao;
import edu.kit.scc.webreg.entity.oauth.OAuthRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthRpFlowStateEntity;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class OAuthClientRedirectServiceImpl implements OAuthClientRedirectService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private OAuthRpConfigurationDao rpConfigDao;

	@Inject
	private RpOAuthFlowStateDao rpFlowStateDao;

	@Override
	@RetryTransaction
	public void redirectClient(Long oidcRelyingPartyId, HttpServletRequest servletRequest, HttpServletResponse response)
			throws OidcAuthenticationException {

		OAuthRpConfigurationEntity rpConfig = rpConfigDao.fetch(oidcRelyingPartyId);

		if (rpConfig == null) {
			throw new OidcAuthenticationException("relying party not configured");
		}

		String callbackUrl;
		if (!rpConfig.getCallbackUrl().startsWith("https://")) {
			/*
			 * we are dealing with a relative acs endpoint. We have to build it with the
			 * called hostname;
			 */
			callbackUrl = "https://" + servletRequest.getServerName() + rpConfig.getCallbackUrl();
		} else {
			callbackUrl = rpConfig.getCallbackUrl();
		}

		redirectClientOauth(rpConfig, callbackUrl, servletRequest, response);
	}

	private void redirectClientOauth(OAuthRpConfigurationEntity rpConfig, String callbackUrl,
			HttpServletRequest servletRequest, HttpServletResponse response) throws OidcAuthenticationException {
		try {
			URI authzEndpoint = URI.create(rpConfig.getServiceUrl());

			ClientID clientID = new ClientID(rpConfig.getClientId());
			Scope scope = new Scope();// OIDCScopeValue.OPENID, OIDCScopeValue.PROFILE, OIDCScopeValue.EMAIL);
			if (rpConfig.getScopes() != null && (!rpConfig.getScopes().equals(""))) {
				String[] scopes = rpConfig.getScopes().split(",");
				for (String s : scopes) {
					scope.add(s.trim());
				}
			}
			URI callback = new URI(callbackUrl);
			State state = new State();
			AuthorizationRequest request = new AuthorizationRequest.Builder(new ResponseType(ResponseType.Value.CODE),
					clientID).scope(scope).state(state).redirectionURI(callback).endpointURI(authzEndpoint).build();
			URI requestURI = request.toURI();

			OAuthRpFlowStateEntity flowState = rpFlowStateDao.createNew();
			flowState.setRpConfiguration(rpConfig);
			flowState.setState(state.getValue());
			rpFlowStateDao.persist(flowState);

			logger.info("Sending OAuth Client to uri: {} with callback {}", requestURI, callbackUrl);

			response.sendRedirect(requestURI.toString());
		} catch (URISyntaxException | IOException e) {
			logger.warn("Exception while building oidc request and redirect: {}", e.getMessage());
			throw new OidcAuthenticationException(e);
		}
	}
}
