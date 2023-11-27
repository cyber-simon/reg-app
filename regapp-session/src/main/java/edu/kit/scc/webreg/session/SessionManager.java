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

import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.service.saml.SamlIdentifier;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

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
	
	private Set<Long> roleIds;
	private Long roleSetCreated;

	private List<Long> serviceApproverList;
	private List<Long> serviceSshPubKeyApproverList;
	private List<Long> serviceAdminList;
	private List<Long> serviceHotlineList;
	private List<Long> serviceGroupAdminList;
	private List<Long> serviceProjectAdminList;

	private List<Long> unregisteredServiceList;
	private Long unregisteredServiceCreated;

	private Set<Long> groupIds;
	private Set<String> groupNames;
	private Long groupSetCreated;
	
	private String theme;
	
	private String locale;
	
	private Instant twoFaElevation;
	private Instant loginTime;
	
	@PostConstruct
	public void init() {
		serviceApproverList = new ArrayList<Long>();
		serviceSshPubKeyApproverList = new ArrayList<Long>();
		serviceAdminList = new ArrayList<Long>();
		serviceHotlineList = new ArrayList<Long>();
		serviceGroupAdminList = new ArrayList<Long>();
		serviceProjectAdminList = new ArrayList<Long>();
		groupIds = new HashSet<Long>();
		groupNames = new HashSet<String>();
		roleIds = new HashSet<Long>();
		loggedInUserList = new HashSet<Long>();
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
		groupIds.clear();
		groupNames.clear();
	}
	
	public boolean isLoggedIn() {
		return (identityId != null ? true : false);		
	}

	public void logout() {
		
	}

	public void addRole(RoleEntity role) {
		roleIds.add(role.getId());
	}
	
	public void addRoles(Set<RoleEntity> rolesToAdd) {
		roleIds.addAll(rolesToAdd.stream().map(role -> role.getId()).toList());
	}

	public boolean isUserInRole(RoleEntity role) {
        return roleIds.contains(role.getId());
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

	public Set<Long> getGroupIds() {
		return groupIds;
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

	public List<Long> getServiceApproverList() {
		return serviceApproverList;
	}

	public List<Long> getServiceAdminList() {
		return serviceAdminList;
	}

	public List<Long> getServiceHotlineList() {
		return serviceHotlineList;
	}

	public List<Long> getServiceGroupAdminList() {
		return serviceGroupAdminList;
	}

	public List<Long> getServiceProjectAdminList() {
		return serviceProjectAdminList;
	}

	public Set<Long> getRoleIds() {
		return roleIds;
	}

	public List<Long> getUnregisteredServiceList() {
		return unregisteredServiceList;
	}

	public void setUnregisteredServiceList(
			List<Long> unregisteredServiceList) {
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

	public List<Long> getServiceSshPubKeyApproverList() {
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
}
