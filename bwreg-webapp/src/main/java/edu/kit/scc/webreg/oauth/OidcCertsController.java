package edu.kit.scc.webreg.oauth;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.Base64URL;

import edu.kit.scc.webreg.entity.oidc.OidcOpConfigurationEntity;
import edu.kit.scc.webreg.service.oidc.OidcOpConfigurationService;
import edu.kit.scc.webreg.service.saml.CryptoHelper;
import edu.kit.scc.webreg.service.saml.exc.OidcAuthenticationException;

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
	public String auth(@PathParam("realm") String realm, @Context HttpServletRequest request, @Context HttpServletResponse response)
			throws IOException, OidcAuthenticationException {
	
		response.setHeader("Access-Control-Allow-Origin", "*");
		
		try {
			logger.debug("certs called for {}", realm);

			OidcOpConfigurationEntity opConfig = opService.findByRealmAndHost(realm, request.getServerName());
			
			if (opConfig == null) {
				throw new OidcAuthenticationException("No such realm");
			}
						
			List<JWK> jwkList = new ArrayList<JWK>();
			if (opConfig.getCertificate() != null && !(opConfig.getCertificate().equals(""))) {
				X509Certificate cert = cryptoHelper.getCertificate(opConfig.getCertificate());
				
				RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
				
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				
				RSAKey rsaKey = new RSAKey.Builder(publicKey)
					.keyUse(KeyUse.SIGNATURE)
					.keyID(cert.getSerialNumber().toString(10))
					.x509CertChain(Collections.singletonList(Base64.encode(cert.getEncoded())))
					.x509CertSHA256Thumbprint(Base64URL.encode(sha256.digest(cert.getEncoded())))
					.build();
					
				jwkList.add(rsaKey);
			}
			if (opConfig.getStandbyCertificate() != null && !(opConfig.getStandbyCertificate().equals(""))) {
				X509Certificate cert = cryptoHelper.getCertificate(opConfig.getStandbyCertificate());

				RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();
				
				MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
				
				RSAKey rsaKey = new RSAKey.Builder(publicKey)
					.keyUse(KeyUse.SIGNATURE)
					.keyID(cert.getSerialNumber().toString(10))
					.x509CertChain(Collections.singletonList(Base64.encode(cert.getEncoded())))
					.x509CertSHA256Thumbprint(Base64URL.encode(sha256.digest(cert.getEncoded())))
					.build();
				jwkList.add(rsaKey);
			}

			JWKSet jwkSet = new JWKSet(jwkList);
			
			return jwkSet.toString(true);
		} catch (NoSuchAlgorithmException | CertificateEncodingException e) {
			throw new OidcAuthenticationException(e);
		}
	}
}
