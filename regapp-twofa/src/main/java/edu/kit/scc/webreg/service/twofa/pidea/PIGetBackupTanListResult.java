package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;

public class PIGetBackupTanListResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean status;
	private PIGetBackupTanListValue value;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public PIGetBackupTanListValue getValue() {
		return value;
	}

	public void setValue(PIGetBackupTanListValue value) {
		this.value = value;
	}

}
