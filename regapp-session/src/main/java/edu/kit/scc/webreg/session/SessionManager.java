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
package edu.kit.scc.webreg.session;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.service.saml.SamlIdentifier;

@Named("sessionManager")
@SessionScoped
public class SessionManager implements Serializable {

	private static final long serialVersionUID = 1L;

	/*
	 * For SAML IDP logins triggered by SP
	 */
	private Long authnRequestId;
	private Long authnRequestIdpConfigId;
	private Long authnRequestSpMetadataId;
	private String authnRequestRelayState;

	/*
	 * For OIDC OP logins triggered by RP
	 */
	private Long oidcFlowStateId;
	private Long oidcAuthnOpConfigId;
	private Long oidcAuthnClientConfigId;
	
	/*
	 * For Local logins to home org SAML IDP
	 */
	private Long idpId;
	private Long spId;
	private String persistentId;
	private SamlIdentifier samlIdentifier;

	/*
	 * For Local logins to home org OIDC OP
	 */
	private Long oidcRelyingPartyId;
	private String subjectId;

	// identityId of the actual user
	private Long identityId;
	// List of logged in account (stored for logout)
	private Set<Long> loggedInUserList;
	
	private String accountLinkingPin;
	
	private Map<String, List<Object>> attributeMap;
	
	private String originalRequestPath;
	private String originalIdpEntityId;
	
	private Set<RoleEntity> roles;
	private Long roleSetCreated;

	private List<ServiceEntity> serviceApproverList;
	private List<ServiceEntity> serviceSshPubKeyApproverList;
	private List<ServiceEntity> serviceAdminList;
	private List<ServiceEntity> serviceHotlineList;
	private List<ServiceEntity> serviceGroupAdminList;
	private List<ServiceEntity> serviceProjectAdminList;

	private List<ServiceEntity> unregisteredServiceList;
	private Long unregisteredServiceCreated;

	private Set<GroupEntity> groups;
	private Set<String> groupNames;
	private Long groupSetCreated;
	
	private Set<ProjectMembershipEntity> projects;
	private Long projectSetCreated;
	
	private String theme;
	
	private String locale;
	
	private Instant twoFaElevation;
	private Instant loginTime;
	
	@PostConstruct
	public void init() {
		serviceApproverList = new ArrayList<ServiceEntity>();
		serviceSshPubKeyApproverList = new ArrayList<ServiceEntity>();
		serviceAdminList = new ArrayList<ServiceEntity>();
		serviceHotlineList = new ArrayList<ServiceEntity>();
		serviceGroupAdminList = new ArrayList<ServiceEntity>();
		serviceProjectAdminList = new ArrayList<ServiceEntity>();
		groups = new HashSet<GroupEntity>();
		groupNames = new HashSet<String>();
		roles = new HashSet<RoleEntity>();
		loggedInUserList = new HashSet<Long>();
		projects = new HashSet<ProjectMembershipEntity>();
	}
	
	public void clearRoleList() {
		serviceApproverList.clear();
		serviceSshPubKeyApproverList.clear();
		serviceAdminList.clear();
		serviceHotlineList.clear();
		serviceGroupAdminList.clear();
		serviceProjectAdminList.clear();
	}
	
	public void clearGroups() {
		groups.clear();
		groupNames.clear();
	}
	
	public void clearProjects() {
		projects.clear();
	}

	public boolean isLoggedIn() {
		return (identityId != null ? true : false);		
	}

	public void logout() {
		
	}

	public void addRole(RoleEntity role) {
		roles.add(role);
	}
	
	public void addRoles(Set<RoleEntity> rolesToAdd) {
		roles.addAll(rolesToAdd);
	}

	public boolean isUserInRole(RoleEntity role) {
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

	public Set<GroupEntity> getGroups() {
		return groups;
	}

	public Long getGroupSetCreated() {
		return groupSetCreated;
	}

	public void setGroupSetCreated(Long groupSetCreated) {
		this.groupSetCreated = groupSetCreated;
	}

	public Set<String> getGroupNames() {
		return groupNames;
	}

	public List<ServiceEntity> getServiceApproverList() {
		return serviceApproverList;
	}

	public List<ServiceEntity> getServiceAdminList() {
		return serviceAdminList;
	}

	public List<ServiceEntity> getServiceHotlineList() {
		return serviceHotlineList;
	}

	public List<ServiceEntity> getServiceGroupAdminList() {
		return serviceGroupAdminList;
	}

	public List<ServiceEntity> getServiceProjectAdminList() {
		return serviceProjectAdminList;
	}

	public Set<RoleEntity> getRoles() {
		return roles;
	}

	public List<ServiceEntity> getUnregisteredServiceList() {
		return unregisteredServiceList;
	}

	public void setUnregisteredServiceList(
			List<ServiceEntity> unregisteredServiceList) {
		this.unregisteredServiceList = unregisteredServiceList;
	}

	public Long getUnregisteredServiceCreated() {
		return unregisteredServiceCreated;
	}

	public void setUnregisteredServiceCreated(Long unregisteredServiceCreated) {
		this.unregisteredServiceCreated = unregisteredServiceCreated;
	}

	public Long getAuthnRequestId() {
		return authnRequestId;
	}

	public void setAuthnRequestId(Long authnRequestId) {
		this.authnRequestId = authnRequestId;
	}

	public Long getAuthnRequestIdpConfigId() {
		return authnRequestIdpConfigId;
	}

	public void setAuthnRequestIdpConfigId(Long authnRequestIdpConfigId) {
		this.authnRequestIdpConfigId = authnRequestIdpConfigId;
	}

	public List<ServiceEntity> getServiceSshPubKeyApproverList() {
		return serviceSshPubKeyApproverList;
	}

	public Instant getTwoFaElevation() {
		return twoFaElevation;
	}

	public void setTwoFaElevation(Instant twoFaElevation) {
		this.twoFaElevation = twoFaElevation;
	}

	public Instant getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(Instant loginTime) {
		this.loginTime = loginTime;
	}

	public Long getIdentityId() {
		return identityId;
	}

	public void setIdentityId(Long identityId) {
		this.identityId = identityId;
	}

	public Long getOidcRelyingPartyId() {
		return oidcRelyingPartyId;
	}

	public void setOidcRelyingPartyId(Long oidcRelyingPartyId) {
		this.oidcRelyingPartyId = oidcRelyingPartyId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public Long getAuthnRequestSpMetadataId() {
		return authnRequestSpMetadataId;
	}

	public void setAuthnRequestSpMetadataId(Long authnRequestSpMetadataId) {
		this.authnRequestSpMetadataId = authnRequestSpMetadataId;
	}

	public Long getOidcFlowStateId() {
		return oidcFlowStateId;
	}

	public void setOidcFlowStateId(Long oidcFlowStateId) {
		this.oidcFlowStateId = oidcFlowStateId;
	}

	public Long getOidcAuthnOpConfigId() {
		return oidcAuthnOpConfigId;
	}

	public void setOidcAuthnOpConfigId(Long oidcAuthnOpConfigId) {
		this.oidcAuthnOpConfigId = oidcAuthnOpConfigId;
	}

	public Long getOidcAuthnClientConfigId() {
		return oidcAuthnClientConfigId;
	}

	public void setOidcAuthnClientConfigId(Long oidcAuthnClientConfigId) {
		this.oidcAuthnClientConfigId = oidcAuthnClientConfigId;
	}

	public String getAccountLinkingPin() {
		return accountLinkingPin;
	}

	public void setAccountLinkingPin(String accountLinkingPin) {
		this.accountLinkingPin = accountLinkingPin;
	}

	public Set<Long> getLoggedInUserList() {
		return loggedInUserList;
	}

	public SamlIdentifier getSamlIdentifier() {
		return samlIdentifier;
	}

	public void setSamlIdentifier(SamlIdentifier samlIdentifier) {
		this.samlIdentifier = samlIdentifier;
	}

	public String getAuthnRequestRelayState() {
		return authnRequestRelayState;
	}

	public void setAuthnRequestRelayState(String authnRequestRelayState) {
		this.authnRequestRelayState = authnRequestRelayState;
	}

	public Set<ProjectMembershipEntity> getProjects() {
		return projects;
	}

	public void setProjects(Set<ProjectMembershipEntity> projects) {
		this.projects = projects;
	}

	public Long getProjectSetCreated() {
		return projectSetCreated;
	}

	public void setProjectSetCreated(Long projectSetCreated) {
		this.projectSetCreated = projectSetCreated;
	}
}
