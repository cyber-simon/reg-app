/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;

@Named("cryptoHelper")
@ApplicationScoped
public class CryptoHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@PostConstruct
	public void init() {
		logger.info("Register BounceyCastle Crypto Provider");
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	public X509Certificate getCertificate(String certString) throws IOException {
		PEMParser pemReader = new PEMParser(new StringReader(certString));
		X509CertificateHolder certHolder = (X509CertificateHolder) pemReader.readObject();
		pemReader.close();
		
		if (certHolder == null) {
			logger.warn("Invalid Certificate. CertHolder is null.");
			return null;
		}

		X509Certificate cert;
		try {
			cert = new JcaX509CertificateConverter().setProvider("BC")
					  .getCertificate(certHolder);
		} catch (CertificateException e) {
			logger.warn("Invalid Certificate", e);
			return null;
		}
		return cert;
	}
	
	public PrivateKey getPrivateKey(String privateKey) throws IOException {
		PEMParser pemReader = new PEMParser(new StringReader(privateKey));
		Object o = pemReader.readObject();
		pemReader.close();

		if (o instanceof PEMKeyPair) {
			PEMKeyPair pemPair = (PEMKeyPair) o;
			KeyPair pair = new JcaPEMKeyConverter().setProvider("BC").getKeyPair(pemPair);
			return pair.getPrivate();
		}
		else if (o instanceof PrivateKeyInfo) {
			PrivateKeyInfo pki = (PrivateKeyInfo) o;
			PrivateKey pk = new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(pki);
			return pk;
		}
		else {
			logger.warn("Cannot load private key of type: {}", o.getClass().getName());
			return null;
		}			
	}


}
