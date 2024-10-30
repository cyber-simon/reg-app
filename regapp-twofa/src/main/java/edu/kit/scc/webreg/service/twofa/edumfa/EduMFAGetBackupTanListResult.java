package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;

public class EduMFAGetBackupTanListResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean status;
	private EduMFAGetBackupTanListValue value;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public EduMFAGetBackupTanListValue getValue() {
		return value;
	}

	public void setValue(EduMFAGetBackupTanListValue value) {
		this.value = value;
	}

}
