package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;
import java.util.Map;

public class EduMFAGetBackupTanListValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, String> otp;
	private String serial;
	private String type;
	private boolean result;

	public Map<String, String> getOtp() {
		return otp;
	}
	
	public void setOtp(Map<String, String> otp) {
		this.otp = otp;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
}
