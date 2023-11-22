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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "RoleEntity")
@Table(name = "role")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class RoleEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", nullable = false, length = 64)
	private String name;

	@OneToMany(targetEntity = UserRoleEntity.class, mappedBy = "role")
	private Set<UserRoleEntity> users;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<UserRoleEntity> getUsers() {
		return users;
	}

	public void setUsers(Set<UserRoleEntity> users) {
		this.users = users;
	}
}
