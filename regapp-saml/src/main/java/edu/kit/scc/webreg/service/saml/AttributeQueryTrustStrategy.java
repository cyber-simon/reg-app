package edu.kit.scc.webreg.service.saml;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Set;

import org.apache.http.conn.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttributeQueryTrustStrategy implements TrustStrategy {

	private static final Logger logger = LoggerFactory.getLogger(AttributeQueryTrustStrategy.class);
	
	private Set<PublicKey> keySet;
	
	public AttributeQueryTrustStrategy(Set<PublicKey> keySet) {
		this.keySet = keySet;
	}
	
	@Override
	public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		logger.debug("Chain is size {}, authTyp is {}", chain.length, authType);
		
		if (chain.length > 0) {
			// Only use first certificate from chain to compare against. 
			X509Certificate x509 = chain[0];
			logger.debug("Examine cert: {}", x509.getSubjectDN());
			try {
				x509.checkValidity();
				for (PublicKey publicKey : keySet) {
					if (x509.getPublicKey().equals(publicKey)) {
						if (logger.isTraceEnabled()) {
							logger.trace("Public key from x509 equals public key from metadata {} = {}", x509.getPublicKey(), publicKey);
						}
						else {
							logger.debug("Public key from x509 equals public key from metadata");
						}
						return true;
					}
					else {
						if (logger.isTraceEnabled()) {
							logger.trace("Public key from x509 does not equal  public key from metadata {} <-> {}", x509.getPublicKey(), publicKey);
						}
						else {
							logger.debug("Public key from x509 does not equal public key from metadata");
						}
					}
				}
			} catch (CertificateExpiredException | CertificateNotYetValidException e) {
				logger.info("Certificate time is not valid or not yet valid (not before: {}, not after: {}), it will be ignored", x509.getNotBefore(), x509.getNotAfter());
			}
		}
			
		return false;
	}
}
