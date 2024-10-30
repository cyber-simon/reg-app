package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;

public class EduMFAAuthTokenValue implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
