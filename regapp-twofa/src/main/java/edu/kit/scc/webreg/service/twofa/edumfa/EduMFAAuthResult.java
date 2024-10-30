package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;

public class EduMFAAuthResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean status;
	private EduMFAAuthTokenValue value;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public EduMFAAuthTokenValue getValue() {
		return value;
	}

	public void setValue(EduMFAAuthTokenValue value) {
		this.value = value;
	}
}
