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
package edu.kit.scc.webreg.entity;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "UserEntity")
@Table(name = "usertable")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UserEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "eppn", length = 1024)
	private String eppn;
	
	@Column(name = "email", length = 1024)
	private String email;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "user_email_addresses")
	private Set<String> emailAddresses;
	
	@Column(name = "given_name", length = 256)
	private String givenName;
	
	@Column(name = "sur_name", length = 256)
	private String surName;

	@Column(name = "last_login_host", length = 1024)
	private String lastLoginHost;

	@Column(name = "uid_number", unique = true, nullable = false)
	private Integer uidNumber;
	
	@OneToMany(targetEntity=UserRoleEntity.class, mappedBy = "user")
	private Set<UserRoleEntity> roles;
	
	@ElementCollection
	@JoinTable(name = "user_store")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 
	
	@ElementCollection
	@JoinTable(name = "user_attribute_store")
    @MapKeyColumn(name = "key_data", length = 1024)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> attributeStore; 

	@OneToMany(targetEntity = UserGroupEntity.class, mappedBy="user")
	private Set<UserGroupEntity> groups;

	@OneToMany(targetEntity = ASUserAttrEntity.class, mappedBy="user")
	private Set<ASUserAttrEntity> userAttrs;
		
	@OneToMany(targetEntity = SshPubKeyEntity.class, mappedBy="user")
	private Set<SshPubKeyEntity> sshPubKeys;
		
	@OneToMany(targetEntity = RegistryEntity.class, mappedBy="user")
	private Set<RegistryEntity> registries;
		
	@Enumerated(EnumType.STRING)
	private UserStatus userStatus;
	
	@Column(name = "last_update")
	private Date lastUpdate;
	
	@Column(name = "scheduled_update")
	private Date scheduledUpdate;
	
	@Column(name = "last_failed_update")
	private Date lastFailedUpdate;
	
	@Column(name = "theme", length = 128)
	private String theme;
	
	@Column(name = "locale", length = 64)
	private String locale;
	
	@ManyToOne(targetEntity = GroupEntity.class)
	private GroupEntity primaryGroup;

	@ManyToOne(targetEntity = IdentityEntity.class)
	private IdentityEntity identity;

	@Column(name = "last_status_change")
	private Date lastStatusChange;
	
	public Set<UserRoleEntity> getRoles() {
		return roles;
	}

	public void setRoles(Set<UserRoleEntity> roles) {
		this.roles = roles;
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

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}

	public Map<String, String> getAttributeStore() {
		return attributeStore;
	}

	public void setAttributeStore(Map<String, String> attributeStore) {
		this.attributeStore = attributeStore;
	}

	public UserStatus getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Integer getUidNumber() {
		return uidNumber;
	}

	public void setUidNumber(Integer uidNumber) {
		this.uidNumber = uidNumber;
	}

	public GroupEntity getPrimaryGroup() {
		return primaryGroup;
	}

	public void setPrimaryGroup(GroupEntity primaryGroup) {
		this.primaryGroup = primaryGroup;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Set<String> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(Set<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	public Set<UserGroupEntity> getGroups() {
		return groups;
	}

	public void setGroups(Set<UserGroupEntity> groups) {
		this.groups = groups;
	}

	public Date getLastStatusChange() {
		return lastStatusChange;
	}

	public void setLastStatusChange(Date lastStatusChange) {
		this.lastStatusChange = lastStatusChange;
	}

	public Date getLastFailedUpdate() {
		return lastFailedUpdate;
	}

	public void setLastFailedUpdate(Date lastFailedUpdate) {
		this.lastFailedUpdate = lastFailedUpdate;
	}

	public Set<ASUserAttrEntity> getUserAttrs() {
		return userAttrs;
	}

	public void setUserAttrs(Set<ASUserAttrEntity> userAttrs) {
		this.userAttrs = userAttrs;
	}

	public Set<SshPubKeyEntity> getSshPubKeys() {
		return sshPubKeys;
	}

	public void setSshPubKeys(Set<SshPubKeyEntity> sshPubKeys) {
		this.sshPubKeys = sshPubKeys;
	}
	
	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	public Date getScheduledUpdate() {
		return scheduledUpdate;
	}

	public void setScheduledUpdate(Date scheduledUpdate) {
		this.scheduledUpdate = scheduledUpdate;
	}

	public String getLastLoginHost() {
		return lastLoginHost;
	}

	public void setLastLoginHost(String lastLoginHost) {
		this.lastLoginHost = lastLoginHost;
	}

	public Set<RegistryEntity> getRegistries() {
		return registries;
	}

	public void setRegistries(Set<RegistryEntity> registries) {
		this.registries = registries;
	}	
}
