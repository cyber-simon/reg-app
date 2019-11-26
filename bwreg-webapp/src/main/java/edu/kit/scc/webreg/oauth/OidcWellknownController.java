package edu.kit.scc.webreg.oauth;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcWellknownController {

	@Inject
	private Logger logger;
	
	@GET
	@Path("/{realm}/.well-known/openid-configuration")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject wellknown(@PathParam("realm") String realm)
			throws ServletException {
		/*
		 * TODO: Realm from configuration (database)
		 */
		
		try {
			List<SubjectType> subjectTypeList = Arrays.asList(new SubjectType[] { SubjectType.PAIRWISE, SubjectType.PUBLIC });
			OIDCProviderMetadata metadata = new OIDCProviderMetadata(new Issuer("https://bwidm.scc.kit.edu/oidc/realms/" + realm), 
					subjectTypeList, new URI("https://bwidm.scc.kit.edu/oidc/realms/" + realm + "/protocol/openid-connect/certs"));

			metadata.setAuthorizationEndpointURI(new URI("https://bwidm.scc.kit.edu/oidc/realms/" + realm + "/protocol/openid-connect/auth"));
			metadata.setTokenEndpointURI(new URI("https://bwidm.scc.kit.edu/oidc/realms/" + realm + "/protocol/openid-connect/token"));
			metadata.setUserInfoEndpointURI(new URI("https://bwidm.scc.kit.edu/oidc/realms/" + realm + "/protocol/openid-connect/userinfo"));
			List<ResponseMode> rms = Arrays.asList(new ResponseMode[] { ResponseMode.QUERY, ResponseMode.FRAGMENT });
			metadata.setResponseModes(rms);

			List<ClientAuthenticationMethod> authMethods = Arrays.asList(new ClientAuthenticationMethod[] { 
					ClientAuthenticationMethod.CLIENT_SECRET_POST, ClientAuthenticationMethod.PRIVATE_KEY_JWT, 
					ClientAuthenticationMethod.CLIENT_SECRET_BASIC } );
			metadata.setTokenEndpointAuthMethods(authMethods);

			List<ResponseType> rts = new ArrayList<ResponseType>();
			rts.add(new ResponseType("code"));
			rts.add(new ResponseType("id_token"));
			rts.add(new ResponseType("code", "id_token"));
			metadata.setResponseTypes(rts);
			
			metadata.setScopes(new Scope("openid", "profile", "email"));
			
			if (logger.isTraceEnabled())
				logger.trace(metadata.toJSONObject().toString());
			
			return metadata.toJSONObject();
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		}
	}	
}
