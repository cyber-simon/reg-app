package edu.kit.scc.webreg.service.twofa;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTwoFaManager implements TwoFaManager {

	private Map<String, String> configMap;

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public void setConfigMap(Map<String, String> configMap) {
		this.configMap = configMap;
	}
}
