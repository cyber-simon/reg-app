package edu.kit.scc.webreg.service.reg;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.codec.binary.Base64;

@ApplicationScoped
public class PasswordUtil {

	
	public String generatePassword(String hashMethod, String password) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance(hashMethod);
		byte[] bytes = password.getBytes(("UTF-8"));
		md.update(bytes);
		byte[] digest = md.digest();
		String hash = "{" + hashMethod + "|" + new String(Base64.encodeBase64(digest)) + "}";
		return hash;
	}

//	public Boolean comparePassword(String password1, String password2) {
//		
//	}
}
