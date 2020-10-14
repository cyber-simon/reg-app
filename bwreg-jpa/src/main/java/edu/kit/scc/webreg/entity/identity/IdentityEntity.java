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
package edu.kit.scc.webreg.entity.identity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "IdentityEntity")
@Table(name = "idty")
public class IdentityEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name="user_pref_name", length=128, unique = true)
	private String userPreferredName;
	
	@Column(name="twofa_user_id", length=512, unique = true)
	private String twoFaUserId;

	@Column(name="twofa_user_name", length=512, unique = true)
	private String twoFaUserName;

	@OneToMany(targetEntity=UserEntity.class, mappedBy = "identity")
	private Set<UserEntity> users;

	public String getUserPreferredName() {
		return userPreferredName;
	}

	public void setUserPreferredName(String userPreferredName) {
		this.userPreferredName = userPreferredName;
	}

	public Set<UserEntity> getUsers() {
		return users;
	}

	public void setUsers(Set<UserEntity> users) {
		this.users = users;
	}

	public String getTwoFaUserId() {
		return twoFaUserId;
	}

	public void setTwoFaUserId(String twoFaUserId) {
		this.twoFaUserId = twoFaUserId;
	}

	public String getTwoFaUserName() {
		return twoFaUserName;
	}

	public void setTwoFaUserName(String twoFaUserName) {
		this.twoFaUserName = twoFaUserName;
	}
}
