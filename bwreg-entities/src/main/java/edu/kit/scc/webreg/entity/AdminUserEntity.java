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

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity(name = "AdminUserEntity")
@Table(name = "adminusertable")
public class AdminUserEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "username", length = 256, unique = true, nullable = false)
	private String username;
	
	@Column(name = "password", length = 256, nullable = false)
	private String password;
	
	@Column(name = "description", length = 512)
	private String description;
	
	@ManyToMany(targetEntity=RoleEntity.class, cascade = CascadeType.ALL)
	@JoinTable(name = "adminuser_role",
			joinColumns = @JoinColumn(name="admin_user_id"),
			inverseJoinColumns = @JoinColumn(name="role_id")
	)
	private Set<RoleEntity> roles;
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<RoleEntity> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleEntity> roles) {
		this.roles = roles;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
