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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
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
	
	private Set<Long> roles;
	private Long roleSetCreated;

	private List<Long> serviceApproverList;
	private List<Long> serviceAdminList;
	private List<Long> serviceHotlineList;
	private List<Long> serviceGroupAdminList;

	private List<Long> groupList;
	private String groupString;
	private Long groupSetCreated;
	
	private String theme;
	
	private String locale;
	
	@PostConstruct
	public void init() {
		serviceApproverList = new ArrayList<Long>();
		serviceAdminList = new ArrayList<Long>();
		serviceHotlineList = new ArrayList<Long>();
		serviceGroupAdminList = new ArrayList<Long>();
		groupList = new ArrayList<Long>();
	}
	
	public void clearRoleList() {
		serviceApproverList.clear();
		serviceAdminList.clear();
		serviceHotlineList.clear();
		serviceGroupAdminList.clear();
	}
	
	public void clearGroupList() {
		groupList.clear();
	}
	
	public Long getUserId() {
		return userId;
	}

	public boolean isLoggedIn() {
		return (userId != null ? true : false);		
	}

	public void logout() {
		
	}

	public void addRole(Long role) {
		if (roles == null) roles = new HashSet<Long>();
		roles.add(role);
	}
	
	public void addRoles(Set<Long> rolesToAdd) {
		if (roles == null) roles = new HashSet<Long>();
		roles.addAll(rolesToAdd);
	}

	public boolean isUserInRole(Long role) {
        return roles.contains(role);
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

	public Long getRoleSetCreated() {
		return roleSetCreated;
	}

	public void setRoleSetCreated(Long roleSetCreated) {
		this.roleSetCreated = roleSetCreated;
	}

	public List<Long> getServiceApproverList() {
		return serviceApproverList;
	}

	public void setServiceApproverList(List<Long> serviceApproverList) {
		this.serviceApproverList = serviceApproverList;
	}

	public List<Long> getServiceAdminList() {
		return serviceAdminList;
	}

	public void setServiceAdminList(List<Long> serviceAdminList) {
		this.serviceAdminList = serviceAdminList;
	}

	public List<Long> getServiceHotlineList() {
		return serviceHotlineList;
	}

	public void setServiceHotlineList(List<Long> serviceHotlineList) {
		this.serviceHotlineList = serviceHotlineList;
	}

	public List<Long> getServiceGroupAdminList() {
		return serviceGroupAdminList;
	}

	public void setServiceGroupAdminList(List<Long> serviceGroupAdminList) {
		this.serviceGroupAdminList = serviceGroupAdminList;
	}

	public List<Long> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<Long> groupList) {
		this.groupList = groupList;
	}

	public String getGroupString() {
		return groupString;
	}

	public void setGroupString(String groupString) {
		this.groupString = groupString;
	}

	public Long getGroupSetCreated() {
		return groupSetCreated;
	}

	public void setGroupSetCreated(Long groupSetCreated) {
		this.groupSetCreated = groupSetCreated;
	}
}
