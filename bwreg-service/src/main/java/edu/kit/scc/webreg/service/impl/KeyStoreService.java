package edu.kit.scc.webreg.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.Map;
import java.util.WeakHashMap;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.KeyStoreDao;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.KeyStoreEntity;
import edu.kit.scc.webreg.entity.KeyStoreEntity_;
import edu.kit.scc.webreg.service.saml.CryptoHelper;

@Stateless
public class KeyStoreService {

	public static final String KEY_ALIAS_SIGNATURE = "signature";
	public static final String KEYSTORE_CONTEXT_EMAIL = "email";

	private static final String KEYSTORE_TYPE = "PKCS12";
	// Placeholders for a correct, configuration-based solution. Credentials MUST NOT be part of the code.
	private static final String KEYSTORE_PWD = "";
	private static final String KEY_ENTRY_PWD = "";

	// Use fetchKeyStore and storeKeyStore to access the key store. Currently no reload is
	// implemented, i.e. it might cause problems in load balancer scenarios.
	private static Map<String, KeyStore> keyStoreCache = new WeakHashMap<>();

	@Inject
	private KeyStoreDao keyStoreDao;

	@Inject
	private CryptoHelper cryptoHelper;

	public void deletePrivateKeyEntry(String context, String alias) {
		KeyStore keyStore = fetchKeyStore(context);
		try {
			keyStore.deleteEntry(alias);
		} catch (KeyStoreException e) {
			throw new IllegalStateException("Could not delete private key entry in key store", e);
		}
		storeKeyStore(context, keyStore);
	}

	public void storePrivateKeyEntry(String context, String alias, String pemPrivateKeyString, String pemCertificateChainString) {
		try {
			storePrivateKeyEntry(context, alias, cryptoHelper.getPrivateKey(pemPrivateKeyString),
					cryptoHelper.getCertificateChain(pemCertificateChainString));
		} catch (IOException e) {
			throw new IllegalStateException("Could not add key to key store", e);
		}
	}

	public void storePrivateKeyEntry(String context, String alias, PrivateKey privateKey, Certificate[] certifcateChain) {
		KeyStore keyStore = fetchKeyStore(context);
		try {
			keyStore.setKeyEntry(alias, privateKey, KEY_ENTRY_PWD.toCharArray(), certifcateChain);
		} catch (KeyStoreException e) {
			throw new IllegalStateException("Could not add key to key store", e);
		}
		storeKeyStore(context, keyStore);
	}

	public synchronized void storeKeyStore(String context, KeyStore keyStore) {
		KeyStoreEntity keyStoreEntity = findKeyStoreEntityByContext(context);
		if (keyStoreEntity == null) {
			keyStoreEntity = new KeyStoreEntity();
			keyStoreEntity.setContext(context);
		}
		keyStoreEntity.setBase64EncodedKeyStoreBlob(encodeBase64(getKeyStoreAsBlob(keyStore)));
		keyStoreDao.persist(keyStoreEntity);
		keyStoreCache.put(context, keyStore);
	}

	private String encodeBase64(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	private byte[] getKeyStoreAsBlob(KeyStore keyStore) {
		try (ByteArrayOutputStream blobStream = new ByteArrayOutputStream()) {
			keyStore.store(blobStream, KEYSTORE_PWD.toCharArray());
			return blobStream.toByteArray();
		} catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
			throw new IllegalStateException("Could not store key store", e);
		}
	}

	public synchronized KeyStore fetchKeyStore(String context) {
		if (keyStoreCache.get(context) == null) {
			KeyStore keyStore = getUninitializedKeyStoreInstance();
			KeyStoreEntity keyStoreEntity = findKeyStoreEntityByContext(context);
			if (keyStoreEntity != null) {
				loadBlobKeyStore(keyStore, decodeBase64(keyStoreEntity.getBase64EncodedKeyStoreBlob()));
			} else {
				loadEmptyKeyStore(keyStore);
			}
			keyStoreCache.put(context, keyStore);
		}
		return keyStoreCache.get(context);
	}

	private KeyStore getUninitializedKeyStoreInstance() {
		try {
			return KeyStore.getInstance(KEYSTORE_TYPE);
		} catch (KeyStoreException e) {
			throw new IllegalStateException("Could not create key store instance", e);
		}
	}

	private KeyStoreEntity findKeyStoreEntityByContext(String context) {
		return keyStoreDao.find(RqlExpressions.equal(KeyStoreEntity_.CONTEXT, context));
	}

	private byte[] decodeBase64(String data) {
		return Base64.getDecoder().decode(data);
	}

	private void loadBlobKeyStore(KeyStore keyStore, byte[] keyStoreBlob) {
		try (ByteArrayInputStream blobStream = new ByteArrayInputStream(keyStoreBlob)) {
			keyStore.load(blobStream, KEYSTORE_PWD.toCharArray());
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new IllegalStateException("Could not load key store", e);
		}
	}

	private void loadEmptyKeyStore(KeyStore keyStore) {
		try {
			keyStore.load(null, null);
		} catch (NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new IllegalStateException("Could not load key store", e);
		}
	}

	public PrivateKeyEntry fetchPrivateKeyEntry(String context, String alias) {
		try {
			Entry entry = fetchKeyStore(context).getEntry(alias, new KeyStore.PasswordProtection(KEY_ENTRY_PWD.toCharArray()));
			return entry instanceof PrivateKeyEntry ? (PrivateKeyEntry) entry : null;
		} catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
			throw new IllegalStateException("Could not get entry from key store", e);
		}
	}

	public boolean hasPrivateKeyEntry(String context, String alias) {
		KeyStore keyStore = fetchKeyStore(context);
		try {
			return keyStore.containsAlias(alias) && keyStore.entryInstanceOf(alias, PrivateKeyEntry.class);
		} catch (KeyStoreException e) {
			throw new IllegalStateException("Could not determine if key store has a private key", e);
		}
	}

}
