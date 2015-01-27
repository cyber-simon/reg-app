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
package edu.kit.lsdf.sns.service;

import java.io.Serializable;

public class PFAccount implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String username;
	private String notes;
	private String custom1, custom2, custom3;
	private String spaceAllowed;
	private String devicesAllowed;
	private String validTil;
	private String serverId;
	private String firstname;
	private String surname;
	private String telephone;
	private String ldapDn;
	private String shibbolethPersistentId;
	private String email;
	private String password;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getNotes() {
		return notes;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getCustom1() {
		return custom1;
	}
	
	public void setCustom1(String custom1) {
		this.custom1 = custom1;
	}
	
	public String getCustom2() {
		return custom2;
	}
	
	public void setCustom2(String custom2) {
		this.custom2 = custom2;
	}
	
	public String getCustom3() {
		return custom3;
	}
	
	public void setCustom3(String custom3) {
		this.custom3 = custom3;
	}
	
	public String getSpaceAllowed() {
		return spaceAllowed;
	}
	
	public void setSpaceAllowed(String spaceAllowed) {
		this.spaceAllowed = spaceAllowed;
	}
	
	public String getDevicesAllowed() {
		return devicesAllowed;
	}
	
	public void setDevicesAllowed(String devicesAllowed) {
		this.devicesAllowed = devicesAllowed;
	}
	
	public String getValidTil() {
		return validTil;
	}
	
	public void setValidTil(String validTil) {
		this.validTil = validTil;
	}
	
	public String getServerId() {
		return serverId;
	}
	
	public void setServerId(String serverId) {
		this.serverId = serverId;
	}
	
	public String getFirstname() {
		return firstname;
	}
	
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	public String getSurname() {
		return surname;
	}
	
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	public String getTelephone() {
		return telephone;
	}
	
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	
	public String getLdapDn() {
		return ldapDn;
	}
	
	public void setLdapDn(String ldapDn) {
		this.ldapDn = ldapDn;
	}
	
	public String getShibbolethPersistentId() {
		return shibbolethPersistentId;
	}
	
	public void setShibbolethPersistentId(String shibbolethPersistentId) {
		this.shibbolethPersistentId = shibbolethPersistentId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
