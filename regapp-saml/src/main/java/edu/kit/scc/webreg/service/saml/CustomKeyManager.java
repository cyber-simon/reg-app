package edu.kit.scc.webreg.service.saml;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509ExtendedKeyManager;

public class CustomKeyManager extends X509ExtendedKeyManager {

	private PrivateKey privateKey;
	private X509Certificate cert;
	
	public CustomKeyManager(X509Certificate cert, PrivateKey privateKey) {
		super();
		this.privateKey = privateKey;
		this.cert = cert;
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] issuers,
			Socket socket) {
		return "RSA";
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] issuers,
			Socket socket) {
		return null;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
		return new X509Certificate[] { cert };
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] issuers) {
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
		return privateKey;
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] issuers) {
		return null;
	}

}
