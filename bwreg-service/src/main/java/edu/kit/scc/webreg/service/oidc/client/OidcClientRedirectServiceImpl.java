package edu.kit.scc.webreg.service.oidc.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;

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
	
	@Override
	public void redirectClient(Long oidcRelyingPartyId, HttpServletResponse response) throws OidcAuthenticationException {
		
		OidcRpConfigurationEntity rpConfig = rpConfigDao.findById(oidcRelyingPartyId);
		
		try {
			URI authzEndpoint = new URI(rpConfig.getAuthUrl());
			ClientID clientID = new ClientID(rpConfig.getClientId());
			Scope scope = new Scope("openid", "profile", "email");
			URI callback = new URI("https://bwidm.scc.kit.edu/rpoidc/callback");
			State state = new State();
			AuthorizationRequest request = new AuthorizationRequest.Builder(
				    new ResponseType(ResponseType.Value.CODE), clientID)
				    .scope(scope)
				    .state(state)
				    .redirectionURI(callback)
				    .endpointURI(authzEndpoint)
				    .build();
			URI requestURI = request.toURI();
			
			OidcRpFlowStateEntity flowState = rpFlowStateDao.createNew();
			flowState.setRpConfiguration(rpConfig);
			flowState.setState(state.getValue());
			rpFlowStateDao.persist(flowState);
			
			logger.info("Sending OIDC Client to uri: {}", requestURI);
			
			response.sendRedirect(requestURI.toString());
		} catch (URISyntaxException | IOException e) {
			logger.warn("Exception while building oidc request and redirect: {}", e.getMessage());
			throw new OidcAuthenticationException(e);
		}
	}
}
