package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.Serializable;

public class EduMFAInitAuthenticatorTokenDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	private EduMFAInitAuthenticatorTokenOtpKey googleurl;
	private String serial;
	private EduMFAInitAuthenticatorTokenOtpKey otpkey;
	
	public EduMFAInitAuthenticatorTokenOtpKey getGoogleurl() {
		return googleurl;
	}
	
	public void setGoogleurl(EduMFAInitAuthenticatorTokenOtpKey googleurl) {
		this.googleurl = googleurl;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public EduMFAInitAuthenticatorTokenOtpKey getOtpkey() {
		return otpkey;
	}

	public void setOtpkey(EduMFAInitAuthenticatorTokenOtpKey otpkey) {
		this.otpkey = otpkey;
	}
	
}
