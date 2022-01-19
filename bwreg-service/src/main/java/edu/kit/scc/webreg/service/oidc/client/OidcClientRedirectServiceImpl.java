package edu.kit.scc.webreg.service.oidc.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;

import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpFlowStateDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcRpFlowStateEntity;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

@Stateless
public class OidcClientRedirectServiceImpl implements OidcClientRedirectService {

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
	public void redirectClient(Long oidcRelyingPartyId, HttpServletRequest servletRequest, HttpServletResponse response) throws OidcAuthenticationException {
		
		OidcRpConfigurationEntity rpConfig = rpConfigDao.findById(oidcRelyingPartyId);
		
		if (rpConfig == null) {
			throw new OidcAuthenticationException("relying party not configured");
		}
		
		String callbackUrl;
		if (! rpConfig.getCallbackUrl().startsWith("https://")) {
			/*
			 * we are dealing with a relative acs endpoint. We have to build it with the called hostname;
			 */
			callbackUrl = "https://" + servletRequest.getServerName() + rpConfig.getCallbackUrl();
		}
		else {
			callbackUrl = rpConfig.getCallbackUrl();
		}
		
		try {
			URI authzEndpoint = opMetadataBean.getAuthorizationEndpointURI(rpConfig);
			
			ClientID clientID = new ClientID(rpConfig.getClientId());
			Scope scope = new Scope();//OIDCScopeValue.OPENID, OIDCScopeValue.PROFILE, OIDCScopeValue.EMAIL);
			String[] scopes = rpConfig.getScopes().split(",");
			for (String s : scopes) {
				scope.add(s.trim());
			}
			URI callback = new URI(callbackUrl);
			State state = new State();
			Nonce nonce = new Nonce();
			AuthenticationRequest request = new AuthenticationRequest.Builder(
				    new ResponseType(ResponseType.Value.CODE),
				    scope, clientID, callback)
				    .state(state)
				    .endpointURI(authzEndpoint)
				    .nonce(nonce)
				    .build();
			URI requestURI = request.toURI();
			
			OidcRpFlowStateEntity flowState = rpFlowStateDao.createNew();
			flowState.setRpConfiguration(rpConfig);
			flowState.setState(state.getValue());
			flowState.setNonce(nonce.getValue());
			rpFlowStateDao.persist(flowState);
			
			logger.info("Sending OIDC Client to uri: {} with callback {}", requestURI, callbackUrl);
			
			response.sendRedirect(requestURI.toString());
		} catch (URISyntaxException | IOException | ParseException e) {
			logger.warn("Exception while building oidc request and redirect: {}", e.getMessage());
			throw new OidcAuthenticationException(e);
		}
	}
}
