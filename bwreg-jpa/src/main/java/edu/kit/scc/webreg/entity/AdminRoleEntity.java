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

@Entity(name = "AdminRoleEntity")
public class AdminRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(targetEntity=ServiceEntity.class, mappedBy="adminRole")
	private Set<ServiceEntity> adminForServices;

	@OneToMany(targetEntity=ServiceEntity.class, mappedBy="hotlineRole")
	private Set<ServiceEntity> hotlineForServices;

	public Set<ServiceEntity> getAdminForServices() {
		return adminForServices;
	}

	public void setAdminForServices(Set<ServiceEntity> adminForServices) {
		this.adminForServices = adminForServices;
	}

	public Set<ServiceEntity> getHotlineForServices() {
		return hotlineForServices;
	}

	public void setHotlineForServices(Set<ServiceEntity> hotlineForServices) {
		this.hotlineForServices = hotlineForServices;
	}
	
}
