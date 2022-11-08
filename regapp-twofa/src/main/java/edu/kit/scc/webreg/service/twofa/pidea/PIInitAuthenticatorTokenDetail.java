package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.Serializable;

public class PIInitAuthenticatorTokenDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	private PIInitAuthenticatorTokenOtpKey googleurl;
	private String serial;
	private PIInitAuthenticatorTokenOtpKey otpkey;
	
	public PIInitAuthenticatorTokenOtpKey getGoogleurl() {
		return googleurl;
	}
	
	public void setGoogleurl(PIInitAuthenticatorTokenOtpKey googleurl) {
		this.googleurl = googleurl;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public PIInitAuthenticatorTokenOtpKey getOtpkey() {
		return otpkey;
	}

	public void setOtpkey(PIInitAuthenticatorTokenOtpKey otpkey) {
		this.otpkey = otpkey;
	}
	
}
