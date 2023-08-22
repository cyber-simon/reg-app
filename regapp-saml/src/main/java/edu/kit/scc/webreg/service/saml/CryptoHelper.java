/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * *****************************************************************************
 */
package edu.kit.scc.webreg.service.saml;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
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
		List<Object> pemObjects = readPemObjects(certString);
		Object pemObject = pemObjects.isEmpty() ? null : pemObjects.get(0);
		X509Certificate certificate = getX509CertificateFromPemObject(pemObject);
		if (certificate == null) {
			logger.warn("Invalid Certificate. Certificate or certifcate holder is null.");
		}
		return certificate;
	}

	private List<Object> readPemObjects(String pemString) throws IOException {
		if (pemString == null) {
			return Collections.emptyList();
		}
		List<Object> pemObjects = new ArrayList<>();
		try (PEMParser pemParser = new PEMParser(new StringReader(pemString))) {
			for (Object pemObject = pemParser.readObject(); pemObject != null; pemObject = pemParser.readObject()) {
				pemObjects.add(pemObject);
			}
		}
		return pemObjects;
	}

	private X509Certificate getX509CertificateFromPemObject(Object pemObject) {
		try {
			return pemObject == null || !X509CertificateHolder.class.isInstance(pemObject) ? null
					: new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) pemObject);
		} catch (CertificateException e) {
			logger.warn("Could not get certificate from pem object", e);
			return null;
		}
	}

	public Certificate[] getCertificateChain(String pemString) throws IOException {
		return readPemObjects(pemString).stream()
				.map(this::getX509CertificateFromPemObject)
				.filter(Objects::nonNull)
				.toArray(size -> new Certificate[size]);
	}

	public PrivateKey getPrivateKey(String privateKey) throws IOException {
		List<Object> pemObjects = readPemObjects(privateKey);
		Object pemObject = pemObjects.isEmpty() ? null : pemObjects.get(0);
		return getPrivateKeyFromPemObject(pemObject);
	}

	private PrivateKey getPrivateKeyFromPemObject(Object pemObject) throws PEMException {
		if (pemObject == null) {
			return null;
		} else if (pemObject instanceof PEMKeyPair) {
			return getPrivateKey((PEMKeyPair) pemObject);
		} else if (pemObject instanceof PrivateKeyInfo) {
			return getPrivateKey((PrivateKeyInfo) pemObject);
		} else {
			logger.warn("Cannot load private key of type: {}", pemObject.getClass().getName());
			return null;
		}
	}

	private PrivateKey getPrivateKey(PEMKeyPair pemKeyPair) throws PEMException {
		return new JcaPEMKeyConverter().setProvider("BC").getKeyPair(pemKeyPair).getPrivate();
	}

	private PrivateKey getPrivateKey(PrivateKeyInfo privateKeyInfo) throws PEMException {
		return new JcaPEMKeyConverter().setProvider("BC").getPrivateKey(privateKeyInfo);
	}

	public String getPemString(Certificate[] certificateChain) {
		StringWriter sWrt = new StringWriter();
		try (JcaPEMWriter pemWriter = new JcaPEMWriter(sWrt)) {
			for (Certificate certificate : certificateChain) {
				pemWriter.writeObject(certificate);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot write certificate chain to PEM file", e);
		}
		return sWrt.toString();
	}

}
