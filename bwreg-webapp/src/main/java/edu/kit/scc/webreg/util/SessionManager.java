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
package edu.kit.scc.webreg.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("sessionManager")
@SessionScoped
public class SessionManager implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long userId;
	
	private Long idpId;
	
	private Long spId;

	private Map<String, List<Object>> attributeMap;
	
	private String persistentId;
	
	private String originalRequestPath;
	private String originalIdpEntityId;
	
	private String assertion;
	
	private Set<String> roles;
	
	private String theme;
	
	private String locale;
	
	public String getAssertion() {
		return assertion;
	}

	public void setAssertion(String assertion) {
		this.assertion = assertion;
	}

	public Long getUserId() {
		return userId;
	}

	public boolean isLoggedIn() {
		return (userId != null ? true : false);		
	}

	public void logout() {
		
	}

	public Long getIdpId() {
		return idpId;
	}

	public void setIdpId(Long idpId) {
		this.idpId = idpId;
	}

	public Long getSpId() {
		return spId;
	}

	public void setSpId(Long spId) {
		this.spId = spId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Map<String, List<Object>> getAttributeMap() {
		return attributeMap;
	}

	public void setAttributeMap(Map<String, List<Object>> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public String getPersistentId() {
		return persistentId;
	}

	public void setPersistentId(String persistentId) {
		this.persistentId = persistentId;
	}
	
    public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public boolean isUserInRole(String role) {
        return roles.contains(role);
    }

	public void setTheme(String theme) {
		this.theme = theme;
	}
	
	public String getTheme() {
		if (theme == null)
			theme = "aristo";
		return theme;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getOriginalIdpEntityId() {
		return originalIdpEntityId;
	}

	public void setOriginalIdpEntityId(String originalIdpEntityId) {
		this.originalIdpEntityId = originalIdpEntityId;
	}

	public String getOriginalRequestPath() {
		return originalRequestPath;
	}

	public void setOriginalRequestPath(String originalRequestPath) {
		this.originalRequestPath = originalRequestPath;
	}	
}
