package edu.kit.scc.webreg.service.reg;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
			return Boolean.FALSE;
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
	
	private String getHashMethod(String hashPassword) {
		if (hashPassword.matches("^{(.*)|(.*)}$")) {
			return hashPassword.split("|")[0].substring(1);
		}
		else if (hashPassword.matches("^{(.*)}(.*)$")) {
			return hashPassword.split("}")[0].substring(1);			
		}
		else
			return null;
	}
}
