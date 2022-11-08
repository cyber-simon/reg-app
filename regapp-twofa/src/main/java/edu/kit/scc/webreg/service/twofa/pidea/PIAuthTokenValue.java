package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;

public class PIAuthTokenValue implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
