package edu.kit.scc.webreg.service.reg;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

@ApplicationScoped
public class PasswordUtil {

	public static final int SALT_LENGTH = 8;
	
	@Inject
	private Logger logger;
	
	private Random random;
	
	@PostConstruct
	public void init() {
		logger.info("Initializing PasswordUtil");
		random = new SecureRandom();
	}
	
	public String generatePassword(String hashMethod, String password) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance(hashMethod);
		byte[] pwBytes = password.getBytes(("UTF-8"));
		byte[] salt = generateSalt();
		byte[] bytes = new byte[pwBytes.length + salt.length];
		System.arraycopy(pwBytes, 0, bytes, 0, pwBytes.length);
		System.arraycopy(salt, 0, bytes, pwBytes.length, salt.length);
		md.update(bytes);
		byte[] digest = md.digest();
		String hash = "{" + hashMethod + "|" + new String(Base64.encodeBase64(digest)) + 
				"|" + new String(Base64.encodeBase64(salt)) + "}";
		return hash;
	}

	public Boolean comparePassword(String plainPassword, String hashPassword) {
		String hashMethod = getHashMethod(hashPassword);
		
		if (hashMethod == null)
			return Boolean.FALSE;
		
		/*
		 * SSHA is the ApacheDS Method. Password hash is fixed length, salt is appended
		 */
		if (hashMethod.equals("SSHA")) {
			String hashPasswordBlank = getPassword(hashPassword);
			byte[] pwAndSalt = Base64.decodeBase64(hashPasswordBlank);
			
			//SHA-1 is 20 bytes long
			int saltLength = pwAndSalt.length - 20;
			byte[] pw = new byte[20];
			byte[] salt = new byte[saltLength];
			System.arraycopy(pwAndSalt, 0, pw, 0, pw.length);
			System.arraycopy(pwAndSalt, pw.length, salt, 0, saltLength);
			
			try {
				byte[] plainPasswordBytes = plainPassword.getBytes("UTF-8");
				byte[] plainPasswordAndSalt = new byte[plainPasswordBytes.length + salt.length];
				System.arraycopy(plainPasswordBytes, 0, plainPasswordAndSalt, 0, plainPasswordBytes.length);
				System.arraycopy(salt, 0, plainPasswordAndSalt, plainPasswordBytes.length, salt.length);

				MessageDigest md = MessageDigest.getInstance("SHA-1");
				md.update(plainPasswordAndSalt);
				byte[] digest = md.digest();

				return Arrays.equals(pw, digest);
				
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				logger.warn("No Algo found", e);
				return Boolean.FALSE;
			}
		}
		/*
		 * Own method, salt is appended base64 encoded after a pipe sign
		 */
		else {
			try {
				String saltString = getSalt(hashPassword);
				String hashPasswordBlank = getPassword(hashPassword);
				
				byte[] pwBytes = plainPassword.getBytes(("UTF-8"));
				byte[] salt = Base64.decodeBase64(saltString);
				byte[] bytes = new byte[pwBytes.length + salt.length];
				System.arraycopy(pwBytes, 0, bytes, 0, pwBytes.length);
				System.arraycopy(salt, 0, bytes, pwBytes.length, salt.length);

				MessageDigest md = MessageDigest.getInstance(hashMethod);
				md.update(bytes);
				byte[] digest = md.digest();

				byte[] hashPasswordBytes = Base64.decodeBase64(hashPasswordBlank);
				
				return Arrays.equals(hashPasswordBytes, digest);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				logger.warn("No Algo found", e);
				return Boolean.FALSE;
			}
		}
	}

	private String getSalt(String hashPassword) {
		if (hashPassword.matches("^\\{(.+)\\|(.+)\\|(.+)\\}$")) {
			return hashPassword.replaceAll("^\\{(.+)\\|(.+)\\|(.+)\\}$", "$3");
		}
		else if (hashPassword.matches("^\\{(.+)\\}(.+)$")) {
			return hashPassword.replaceAll("^\\{(.+)\\}(.+)$", "$2");
		}
		else
			return null;
	}

	private String getPassword(String hashPassword) {
		if (hashPassword.matches("^\\{(.+)\\|(.+)\\|(.+)\\}$")) {
			return hashPassword.replaceAll("^\\{(.+)\\|(.+)\\|(.+)\\}$", "$2");
		}
		else if (hashPassword.matches("^\\{(.+)\\}(.+)$")) {
			return hashPassword.replaceAll("^\\{(.+)\\}(.+)$", "$2");
		}
		else
			return null;
	}
	
	private String getHashMethod(String hashPassword) {
		if (hashPassword.matches("^\\{(.+)\\|(.+)\\|(.+)\\}$")) {
			return hashPassword.replaceAll("^\\{(.+)\\|(.+)\\|(.+)\\}$", "$1");
		}
		else if (hashPassword.matches("^\\{(.+)\\}(.+)$")) {
			return hashPassword.replaceAll("^\\{(.+)\\}(.+)$", "$1");
		}
		else
			return null;
	}
	
	private byte[] generateSalt() {
		byte[] salt = new byte[SALT_LENGTH];
		random.nextBytes(salt);
		return salt;
	}
}
