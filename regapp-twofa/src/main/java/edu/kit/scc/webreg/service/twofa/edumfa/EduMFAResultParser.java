package edu.kit.scc.webreg.service.twofa.edumfa;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.scc.webreg.service.twofa.TwoFaException;

public class EduMFAResultParser {

	private ObjectMapper om;
	
	public EduMFAResultParser() {
		om = new ObjectMapper();
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public EduMFASimpleResponse parseSimpleResponse(String responseString) throws TwoFaException {
		try {
			EduMFASimpleResponse response = om.readValue(responseString, EduMFASimpleResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public EduMFAAuthResponse parseAuthResponse(String responseString) throws TwoFaException {
		try {
			EduMFAAuthResponse response = om.readValue(responseString, EduMFAAuthResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public EduMFAShowUserResponse parseShowUserResponse(String responseString) throws TwoFaException {
		try {
			EduMFAShowUserResponse response = om.readValue(responseString, EduMFAShowUserResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

	public EduMFAInitAuthenticatorTokenResponse parseInitAuthenticatorTokenResponse(String responseString) throws TwoFaException {
		try {
			EduMFAInitAuthenticatorTokenResponse response = 
					om.readValue(responseString, EduMFAInitAuthenticatorTokenResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}	

	public EduMFAInitPaperTanTokenResponse parseInitPaperTanTokenResponse(String responseString) throws TwoFaException {
		try {
			EduMFAInitPaperTanTokenResponse response = 
					om.readValue(responseString, EduMFAInitPaperTanTokenResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}	
	
	public EduMFAGetBackupTanListResponse parseGetBackupTanListResponse(String responseString) throws TwoFaException {
		try {
			EduMFAGetBackupTanListResponse response = 
					om.readValue(responseString, EduMFAGetBackupTanListResponse.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public EduMFASetFieldResult parseSetFieldResponse(String responseString) throws TwoFaException {
		try {
			EduMFASetFieldResult response = 
					om.readValue(responseString, EduMFASetFieldResult.class);
			return response;
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}

}
