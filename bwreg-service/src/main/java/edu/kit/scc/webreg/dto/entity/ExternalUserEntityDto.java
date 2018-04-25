package edu.kit.scc.webreg.dto.entity;

import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import edu.kit.scc.webreg.entity.UserStatus;

public class ExternalUserEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	private String externalId;
	
    private Map<String, String> attributeStore; 

    private Map<String, String> genericStore;

    private String eppn;

	private String email;
	
	private String givenName;
	
	private String surName;
	
	private Integer uidNumber;
	
	private Set<String> emailAddresses;

	private GroupEntityDto primaryGroup;
	
	private Set<GroupEntityDto> secondaryGroups;
	
	private UserStatus userStatus;
	
	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Map<String, String> getAttributeStore() {
		return attributeStore;
	}

	public void setAttributeStore(Map<String, String> attributeStore) {
		this.attributeStore = attributeStore;
	}

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}

	public String getEppn() {
		return eppn;
	}

	public void setEppn(String eppn) {
		this.eppn = eppn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<String> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(Set<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
	}

	public Integer getUidNumber() {
		return uidNumber;
	}

	public void setUidNumber(Integer uidNumber) {
		this.uidNumber = uidNumber;
	}

	public GroupEntityDto getPrimaryGroup() {
		return primaryGroup;
	}

	public void setPrimaryGroup(GroupEntityDto primaryGroup) {
		this.primaryGroup = primaryGroup;
	}

	public Set<GroupEntityDto> getSecondaryGroups() {
		return secondaryGroups;
	}

	public void setSecondaryGroups(Set<GroupEntityDto> secondaryGroups) {
		this.secondaryGroups = secondaryGroups;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}
	

}
