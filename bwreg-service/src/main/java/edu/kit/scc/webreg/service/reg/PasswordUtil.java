package edu.kit.scc.webreg.service.reg;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

@ApplicationScoped
public class PasswordUtil {

	@Inject
	private Logger logger;
	
	public String generatePassword(String hashMethod, String password) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance(hashMethod);
		byte[] bytes = password.getBytes(("UTF-8"));
		md.update(bytes);
		byte[] digest = md.digest();
		String hash = "{" + hashMethod + "|" + new String(Base64.encodeBase64(digest)) + "}";
		return hash;
	}

	public Boolean comparePassword(String plainPassword, String hashPassword) {
		String hashMethod = getHashMethod(hashPassword);
		
		if (hashMethod == null)
			return Boolean.FALSE;
		
		if (hashMethod.equals("SSHA")) {
			//@TODO Implement apacheds style salted sha-1
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
		else {
			String comparePassword;
			try {
				comparePassword = generatePassword(hashMethod, plainPassword);
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				logger.warn("No Algo found", e);
				return Boolean.FALSE;
			}
			return comparePassword.equals(hashPassword);
		}
	}
	
	private String getPassword(String hashPassword) {
		if (hashPassword.matches("^\\{(.+)\\|(.+)\\}$")) {
			return hashPassword.replaceAll("^\\{(.+)\\|(.+)\\}$", "$2");
		}
		else if (hashPassword.matches("^\\{(.+)\\}(.+)$")) {
			return hashPassword.replaceAll("^\\{(.+)\\}(.+)$", "$2");
		}
		else
			return null;
	}
	
	private String getHashMethod(String hashPassword) {
		if (hashPassword.matches("^\\{(.+)\\|(.+)\\}$")) {
			return hashPassword.replaceAll("^\\{(.+)\\|(.+)\\}$", "$1");
		}
		else if (hashPassword.matches("^\\{(.+)\\}(.+)$")) {
			return hashPassword.replaceAll("^\\{(.+)\\}(.+)$", "$1");
		}
		else
			return null;
	}
}
