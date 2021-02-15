package edu.kit.scc.webreg.oauth;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;
import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;
import net.minidev.json.JSONObject;

@Path("/realms")
public class OidcCertsController {

	@Inject
	private Logger logger;
	
	@Inject
	private OidcOpConfigurationService opService;
	
	@Inject 
	private CryptoHelper cryptoHelper;
	
	@GET
	@Path("/{realm}/protocol/openid-connect/certs")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject auth(@PathParam("realm") String realm, @Context HttpServletRequest request, @Context HttpServletResponse response)
			throws IOException, OidcAuthenticationException {
		
		try {
			logger.debug("certs called for {}", realm);

			OidcOpConfigurationEntity opConfig = opService.findByRealmAndHost(realm, request.getLocalName());
			
			if (opConfig == null) {
				throw new OidcAuthenticationException("No such realm");
			}
						
			List<JWK> jwkList = new ArrayList<JWK>();
			if (opConfig.getCertificate() != null && !(opConfig.getCertificate().equals(""))) {
				X509Certificate certificate = cryptoHelper.getCertificate(opConfig.getCertificate());
				JWK jwk = JWK.parse(certificate);
				jwkList.add(jwk);
			}
			if (opConfig.getStandbyCertificate() != null && !(opConfig.getStandbyCertificate().equals(""))) {
				X509Certificate certificate = cryptoHelper.getCertificate(opConfig.getStandbyCertificate());
				JWK jwk = JWK.parse(certificate);
				jwkList.add(jwk);
			}
			
			JWKSet jwkSet = new JWKSet(jwkList);
			
			return jwkSet.toJSONObject(true);
		} catch (JOSEException e) {
			throw new OidcAuthenticationException(e);
		}
	}
}
