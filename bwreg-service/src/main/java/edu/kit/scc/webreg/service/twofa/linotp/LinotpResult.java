package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.Serializable;

public class LinotpResult implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean status;
	private boolean value;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public boolean isValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

}
