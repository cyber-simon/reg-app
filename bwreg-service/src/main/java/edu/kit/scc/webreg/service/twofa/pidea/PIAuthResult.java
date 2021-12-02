package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;

public class PIAuthResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean status;
	private PIAuthTokenValue value;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public PIAuthTokenValue isValue() {
		return value;
	}

	public void setValue(PIAuthTokenValue value) {
		this.value = value;
	}
}
