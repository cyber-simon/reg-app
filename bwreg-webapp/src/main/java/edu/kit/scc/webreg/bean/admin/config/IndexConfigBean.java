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
package edu.kit.scc.webreg.bean.admin.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;

@ManagedBean
@ViewScoped
public class IndexConfigBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApplicationConfig appConfig;
	
	private Boolean initialized = false;
	
	private List<String> keyList;
	private Map<String, String> configMap;
	
	private String newKey, newValue;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			init();
			initialized = true;
		}
	}
	
	private void init() {
		configMap = appConfig.getConfigOptions();
		keyList = new ArrayList<String>(configMap.keySet());
		Collections.sort(keyList);
	}
	
	public void scheduleReload() {
		appConfig.scheduleReload();
	}
	
	public void saveKey(String key) {
		if (configMap.get(key) == null || configMap.get(key).equals("")) {
			appConfig.deleteConfigValue(key);
		}
		else {
			appConfig.storeConfigValue(key, configMap.get(key));
		}
		
		init();
	}

	public void addKey() {
		if (newKey != null && newValue != null)
			appConfig.storeConfigValue(newKey, newValue);
		
		newKey = null;
		newValue = null;
		
		init();
	}

	public List<String> getKeyList() {
		return keyList;
	}

	public Map<String, String> getConfigMap() {
		return configMap;
	}

	public String getNewKey() {
		return newKey;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public Date getLastLoad() {
		return appConfig.getLastLoad();
	}
	
	public boolean getScheduledReload() {
		if (appConfig.getLastLoad() == null || appConfig.getNextScheduledReload() == null)
			return false;
		else
			return (appConfig.getLastLoad().compareTo(appConfig.getNextScheduledReload()) < 0);
	}
}
