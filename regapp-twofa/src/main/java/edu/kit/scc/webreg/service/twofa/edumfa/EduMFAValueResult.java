package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;

public class EduMFAValueResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean status;

	private EduMFAValue value;
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public EduMFAValue getValue() {
		return value;
	}

	public void setValue(EduMFAValue value) {
		this.value = value;
	}
	
	
}
