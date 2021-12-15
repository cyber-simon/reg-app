package edu.kit.scc.webreg.service.twofa.token;

import java.util.Map;

public class HmacTokenList extends GenericTwoFaToken {

	private static final long serialVersionUID = 1L;

	private Map<String, String> otp;

	public Map<String, String> getOtp() {
		return otp;
	}

	public void setOtp(Map<String, String> otp) {
		this.otp = otp;
	}	
}
