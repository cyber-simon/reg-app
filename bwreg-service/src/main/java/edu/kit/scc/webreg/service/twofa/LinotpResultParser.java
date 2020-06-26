package edu.kit.scc.webreg.service.twofa;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.scc.webreg.service.twofa.linotp.LinotpResponse;

public class LinotpResultParser {

	private ObjectMapper om;
	private LinotpResponse response;
	
	public LinotpResultParser() {
		om = new ObjectMapper();
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public void parseResult(String responseString) throws TwoFaException {
		try {
			response = om.readValue(responseString, LinotpResponse.class);
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public LinotpResponse getResponse() {
		return response;
	}
}
