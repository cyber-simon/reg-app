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

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity(name = "GroupAdminRoleEntity")
public class GroupAdminRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(targetEntity=GroupEntity.class, mappedBy="adminRoles")
	private Set<ServiceEntity> adminForGroups;

	public Set<ServiceEntity> getAdminForGroups() {
		return adminForGroups;
	}

	public void setAdminForGroups(Set<ServiceEntity> adminForGroups) {
		this.adminForGroups = adminForGroups;
	}
}
