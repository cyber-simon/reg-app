package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.Serializable;

public class LinotpValueResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean status;

	private LinotpValue value;
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public LinotpValue getValue() {
		return value;
	}

	public void setValue(LinotpValue value) {
		this.value = value;
	}
	
	
}
