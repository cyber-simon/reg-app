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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class EditAccessRuleConfigBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApplicationConfig appConfig;
	
	private Boolean initialized = false;
	
	private String userLoginRule;
	private String userFirstRegisterRule;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			userLoginRule = appConfig.getConfigValue("user_login_rule");
			userFirstRegisterRule = appConfig.getConfigValue("user_first_register_rule");
			initialized = true;
		}
	}
	
	public String save() {
		if (userLoginRule == null || userLoginRule.equals("")) {
			appConfig.deleteConfigValue("user_login_rule");
		}
		else {
			appConfig.storeConfigValue("user_login_rule", userLoginRule);
		}
		
		if (userFirstRegisterRule == null || userFirstRegisterRule.equals("")) {
			appConfig.deleteConfigValue("user_first_register_rule");
		}
		else {
			appConfig.storeConfigValue("user_first_register_rule", userFirstRegisterRule);
		}
		
		return ViewIds.CONFIG_INDEX + "?faces-redirect=true";
	}

	public String cancel() {
		return ViewIds.CONFIG_INDEX + "?faces-redirect=true";
	}

	public String getUserLoginRule() {
		return userLoginRule;
	}

	public void setUserLoginRule(String userLoginRule) {
		this.userLoginRule = userLoginRule;
	}

	public String getUserFirstRegisterRule() {
		return userFirstRegisterRule;
	}

	public void setUserFirstRegisterRule(String userFirstRegisterRule) {
		this.userFirstRegisterRule = userFirstRegisterRule;
	}

}
