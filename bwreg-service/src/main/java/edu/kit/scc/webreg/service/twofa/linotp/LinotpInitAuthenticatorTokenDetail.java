package edu.kit.scc.webreg.service.twofa.linotp;

import java.io.Serializable;

public class LinotpInitAuthenticatorTokenDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	private LinotpInitAuthenticatorTokenOtpKey googleurl;
	private String serial;
	private LinotpInitAuthenticatorTokenOtpKey otpkey;
	
	public LinotpInitAuthenticatorTokenOtpKey getGoogleurl() {
		return googleurl;
	}
	
	public void setGoogleurl(LinotpInitAuthenticatorTokenOtpKey googleurl) {
		this.googleurl = googleurl;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public LinotpInitAuthenticatorTokenOtpKey getOtpkey() {
		return otpkey;
	}

	public void setOtpkey(LinotpInitAuthenticatorTokenOtpKey otpkey) {
		this.otpkey = otpkey;
	}
	
}
