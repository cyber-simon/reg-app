package edu.kit.scc.webreg.oauth;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

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
	public JSONObject wellknown(@PathParam("realm") String serviceShortName)
			throws ServletException {
		try {
			List<SubjectType> subjectTypeList = new ArrayList<SubjectType>();
			subjectTypeList.add(SubjectType.PAIRWISE);
			subjectTypeList.add(SubjectType.PUBLIC);
			OIDCProviderMetadata metadata = new OIDCProviderMetadata(new Issuer("https://bwidm.scc.kit.edu/oidc/auth/realms/bwidm"), 
					subjectTypeList, new URI("https://bwidm.scc.kit.edu/oidc/jwk"));

			logger.debug(metadata.toJSONObject().toString());
			return metadata.toJSONObject();
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		}
	}	
}
