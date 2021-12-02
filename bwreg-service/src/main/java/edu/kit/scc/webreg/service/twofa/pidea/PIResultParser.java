package edu.kit.scc.webreg.service.twofa.pidea;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.scc.webreg.service.twofa.TwoFaException;

public class PIResultParser {

	private ObjectMapper om;
	
	public PIResultParser() {
		om = new ObjectMapper();
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public PISimpleResponse parseSimpleResponse(String responseString) throws TwoFaException {
		try {
			PISimpleResponse response = om.readValue(responseString, PISimpleResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public PIAuthResponse parseAuthResponse(String responseString) throws TwoFaException {
		try {
			PIAuthResponse response = om.readValue(responseString, PIAuthResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public PIShowUserResponse parseShowUserResponse(String responseString) throws TwoFaException {
		try {
			PIShowUserResponse response = om.readValue(responseString, PIShowUserResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public PIInitAuthenticatorTokenResponse parseInitAuthenticatorTokenResponse(String responseString) throws TwoFaException {
		try {
			PIInitAuthenticatorTokenResponse response = 
					om.readValue(responseString, PIInitAuthenticatorTokenResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}	
	
	public PIGetBackupTanListResponse parseGetBackupTanListResponse(String responseString) throws TwoFaException {
		try {
			PIGetBackupTanListResponse response = 
					om.readValue(responseString, PIGetBackupTanListResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public PISetFieldResult parseSetFieldResponse(String responseString) throws TwoFaException {
		try {
			PISetFieldResult response = 
					om.readValue(responseString, PISetFieldResult.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

}
