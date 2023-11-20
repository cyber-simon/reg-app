package edu.kit.scc.webreg.dto.entity;

import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import edu.kit.scc.webreg.entity.UserStatus;

public class UserEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

    private Map<String, String> attributeStore; 

    private Map<String, String> genericStore;

    @Pattern(message = "Invalid EPPN",
            regexp = "^[a-zA-Z0-9_!#$%&*+/=?{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @Size(min = 1, max = 1023, message = "The length of EPPN should be between 1 to 1023")
    private String eppn;

    @Pattern(message = "Invalid Email Address",
            regexp = "^[a-zA-Z0-9_!#$%&*+/=?{|}~^.-]+@[a-zA-Z0-9.-]+$")
    @Size(min = 1, max = 1023, message = "The length of email should be between 1 to 1023")
    private String email;
	
    @Size(min = 1, max = 255, message = "The length of givenName should be between 1 to 255")
	private String givenName;
	
    @Size(min = 1, max = 255, message = "The length of surName should be between 1 to 255")
	private String surName;
	
	private Integer uidNumber;
	
	private Set<String> emailAddresses;

	private GroupEntityDto primaryGroup;
	
	private Set<GroupEntityDto> secondaryGroups;
	
	private UserStatus userStatus;
	
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
