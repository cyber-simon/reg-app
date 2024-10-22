/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.bootstrap;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.ApplicationConfigDao;
import edu.kit.scc.webreg.entity.ApplicationConfigEntity;

@ApplicationScoped
public class ApplicationConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private ApplicationConfigDao dao;

	private ApplicationConfigEntity appConfig;

	private Date lastLoad;

	private Map<String, String> localeMap;

	public void init() {
		logger.debug("Checking for Active Configuration");
		appConfig = dao.findActive();

		if (appConfig == null) {
			logger.info("No active configuration found. Creating new config");
			appConfig = dao.createNew();
			appConfig.setConfigFormatVersion("2.0.0");
			appConfig.setSubVersion("1");
			appConfig.setActiveConfig(true);
			appConfig.setConfigOptions(new HashMap<String, String>());
			appConfig = dao.persist(appConfig);
		}

		lastLoad = new Date();

		localeMap = new HashMap<String, String>();
		localeMap.put("en", "English");
		localeMap.put("de", "Deutsch");
		localeMap.put("fr", "Fran\u00E7ais");
	}

	public boolean reload() {
		ApplicationConfigEntity newAppConfig = dao.findReloadActive(lastLoad);
		boolean reload = newAppConfig != null;
		if (reload) {
			logger.info("Reloading Application Configuration");
			appConfig = newAppConfig;
			lastLoad = new Date();
		}
		return reload;
	}

	public void scheduleReload() {
		appConfig.setDirtyStamp(new Date());
		appConfig = dao.persist(appConfig);
	}

	public String getConfigValue(String key) {
		return appConfig.getConfigOptions().get(key);
	}

	public String getConfigValueOrDefault(String key, String defaultValue) {
		if (appConfig.getConfigOptions().containsKey(key)) {
			return getConfigValue(key);
		} else {
			return defaultValue;
		}
	}

	public String deleteConfigValue(String key) {
		String value = appConfig.getConfigOptions().remove(key);
		appConfig = dao.persist(appConfig);
		return value;
	}

	public void storeConfigValue(String key, String value) {
		appConfig.getConfigOptions().put(key, value);
		appConfig = dao.persist(appConfig);
	}

	public Map<String, String> getConfigOptions() {
		return appConfig.getConfigOptions();
	}

	public Date getLastLoad() {
		return lastLoad;
	}

	public Date getNextScheduledReload() {
		return appConfig.getDirtyStamp();
	}

	public Map<String, String> getLocaleMap() {
		return localeMap;
	}
}
