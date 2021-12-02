package edu.kit.scc.webreg.service.twofa.token;

public class TotpToken extends GenericTwoFaToken {

	private static final long serialVersionUID = 1L;

	private String otpLen;
	private String countWindow;

	public String getOtpLen() {
		return otpLen;
	}

	public void setOtpLen(String otpLen) {
		this.otpLen = otpLen;
	}

	public String getCountWindow() {
		return countWindow;
	}

	public void setCountWindow(String countWindow) {
		this.countWindow = countWindow;
	}

}
