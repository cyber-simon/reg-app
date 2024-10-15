package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;
import java.util.Map;

public class EduMFASetFieldResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean status;
	private Map<String, Object> value;

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Map<String, Object> getValue() {
		return value;
	}

	public void setValue(Map<String, Object> value) {
		this.value = value;
	}
}