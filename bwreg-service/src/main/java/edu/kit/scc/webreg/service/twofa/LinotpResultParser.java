package edu.kit.scc.webreg.service.twofa;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpShowUserResponse;

public class LinotpResultParser {

	private ObjectMapper om;
	
	public LinotpResultParser() {
		om = new ObjectMapper();
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public LinotpShowUserResponse parseShowUserResponse(String responseString) throws TwoFaException {
		try {
			LinotpShowUserResponse response = om.readValue(responseString, LinotpShowUserResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public LinotpInitAuthenticatorTokenResponse parseInitAuthenticatorTokenResponse(String responseString) throws TwoFaException {
		try {
			LinotpInitAuthenticatorTokenResponse response = 
					om.readValue(responseString, LinotpInitAuthenticatorTokenResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

}
