package edu.kit.scc.webreg.service.twofa;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LinotpResultParser {

	private ObjectMapper om;
	private Map<String, Object> resultMap;
	
	public LinotpResultParser() {
		om = new ObjectMapper();
	}
	
	public void parseResult(String responseString) throws TwoFaException {
		try {
			resultMap = om.readValue(responseString, new TypeReference<Map<String, Object>>() {});
		} catch (IOException e) {
			throw new TwoFaException(e);
		}		
	}
	
	public Boolean getResultStatus() {
		if (resultMap.containsKey("result") && (resultMap.get("result") instanceof Boolean)) {
			return (Boolean) resultMap.get("result");
		}
		else {
			return false;
		}
	}
}
