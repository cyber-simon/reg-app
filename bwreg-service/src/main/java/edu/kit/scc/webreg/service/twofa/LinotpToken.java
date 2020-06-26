package edu.kit.scc.webreg.service.twofa;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LinotpToken implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serial;
	
	private Map<String, Object> valueMap;

	private Boolean readOnly;
	
	public LinotpToken() {
		valueMap = new HashMap<String, Object>();
	}
	
	public String getSerial() {
		return serial;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public Map<String, Object> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<String, Object> valueMap) {
		this.valueMap = valueMap;
	}
	
}
