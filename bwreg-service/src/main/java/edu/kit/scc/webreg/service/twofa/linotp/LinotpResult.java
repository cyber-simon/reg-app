package edu.kit.scc.webreg.service.twofa.linotp;

public class LinotpResult {

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
