package edu.kit.scc.webreg.service.oidc.client;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationResponse;
import com.nimbusds.oauth2.sdk.AuthorizationSuccessResponse;
import com.nimbusds.oauth2.sdk.ParseException;

import edu.kit.scc.webreg.dao.oidc.OidcRpConfigurationDao;
import edu.kit.scc.webreg.dao.oidc.OidcRpFlowStateDao;
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
	
			AuthorizationSuccessResponse successResponse = (AuthorizationSuccessResponse)response;
	
			OidcRpFlowStateEntity flowState = rpFlowStateDao.findByState(successResponse.getState().getValue());
	
			// The returned state parameter must match the one send with the request
			if (flowState == null) {
				throw new OidcAuthenticationException("State is wrong or expired");
			}
	
			AuthorizationCode code = successResponse.getAuthorizationCode();
			flowState.setCode(code.getValue());

		} catch (ParseException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
