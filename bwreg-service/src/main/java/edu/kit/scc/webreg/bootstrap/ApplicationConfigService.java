package edu.kit.scc.webreg.bootstrap;

import java.util.Date;
import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class ApplicationConfigService {

	@Inject
	private ApplicationConfig appConfig;

	public String getConfigValueOrDefault(String key, String defaultValue) {
		return appConfig.getConfigValueOrDefault(key, defaultValue);
	}

	public String deleteConfigValue(String key) {
		return appConfig.deleteConfigValue(key);
	}

	public void storeConfigValue(String key, String value) {
		appConfig.storeConfigValue(key, value);
	}

	public void scheduleReload() {
		appConfig.scheduleReload();
	}

	public Map<String, String> getConfigOptions() {
		return appConfig.getConfigOptions();
	}

	public Date getLastLoad() {
		return appConfig.getLastLoad();
	}

	public Date getNextScheduledReload() {
		return appConfig.getNextScheduledReload();
	}
}
