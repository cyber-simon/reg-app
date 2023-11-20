package edu.kit.scc.webreg.oauth;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.ResponseMode;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;
import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcWellknownController {

	@Inject
	private Logger logger;
	
	@Inject
	private OidcOpConfigurationService opService;
	
	@GET
	@Path("/{realm}/.well-known/openid-configuration")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject wellknown(@PathParam("realm") String realm, @Context HttpServletRequest request, @Context HttpServletResponse response)
			throws ServletException {

		response.setHeader("Access-Control-Allow-Origin", "*");
		
		OidcOpConfigurationEntity opConfig = opService.findByRealmAndHost(realm, request.getServerName());
		
		if (opConfig == null) {
			throw new ServletException("No such realm");
		}
			
		try {
			List<SubjectType> subjectTypeList = Arrays.asList(new SubjectType[] { SubjectType.PAIRWISE, SubjectType.PUBLIC });
			OIDCProviderMetadata metadata = new OIDCProviderMetadata(new Issuer("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm()), 
					subjectTypeList, new URI("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/certs"));

			metadata.setAuthorizationEndpointURI(new URI("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/auth"));
			metadata.setTokenEndpointURI(new URI("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/token"));
			metadata.setUserInfoEndpointURI(new URI("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/userinfo"));
			metadata.setIntrospectionEndpointURI(new URI("https://" + opConfig.getHost() + "/oidc/realms/" + opConfig.getRealm() + "/protocol/openid-connect/tokeninfo"));
			List<ResponseMode> rms = Arrays.asList(new ResponseMode[] { ResponseMode.QUERY, ResponseMode.FRAGMENT });
			metadata.setResponseModes(rms);

			List<ClientAuthenticationMethod> authMethods = Arrays.asList(new ClientAuthenticationMethod[] { 
					ClientAuthenticationMethod.CLIENT_SECRET_POST, ClientAuthenticationMethod.PRIVATE_KEY_JWT, 
					ClientAuthenticationMethod.CLIENT_SECRET_BASIC } );
			metadata.setTokenEndpointAuthMethods(authMethods);

			List<GrantType> gts = new ArrayList<GrantType>();
			gts.add(GrantType.AUTHORIZATION_CODE);
			gts.add(GrantType.REFRESH_TOKEN);
			metadata.setGrantTypes(gts);
			
			List<ResponseType> rts = new ArrayList<ResponseType>();
			rts.add(new ResponseType("code"));
			rts.add(new ResponseType("id_token"));
			rts.add(new ResponseType("code", "id_token"));
			metadata.setResponseTypes(rts);
			
			List<JWSAlgorithm> idTokenJWSAlgs = new ArrayList<JWSAlgorithm>();
			idTokenJWSAlgs.add(JWSAlgorithm.RS256);
			metadata.setIDTokenJWSAlgs(idTokenJWSAlgs);
			
			metadata.setScopes(new Scope("openid", "profile", "email"));
			
			metadata.setClaims(Arrays.asList(new String[] { "sub", "iss", "aud", "mail", "name" }));
			
			if (logger.isTraceEnabled())
				logger.trace(metadata.toJSONObject().toString());
			
			return metadata.toJSONObject();
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		}
	}	
}
