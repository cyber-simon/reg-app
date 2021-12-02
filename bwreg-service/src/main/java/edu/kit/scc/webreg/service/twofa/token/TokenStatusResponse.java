package edu.kit.scc.webreg.service.twofa.token;

import java.io.Serializable;

public class TokenStatusResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serial;
	private Boolean success;
	private String message;
	
	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
