package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;

public class PIValueResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean status;

	private PIValue value;
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public PIValue getValue() {
		return value;
	}

	public void setValue(PIValue value) {
		this.value = value;
	}
	
	
}
