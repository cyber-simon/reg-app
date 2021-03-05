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

@Entity(name = "SamlIdpAdminRoleEntity")
public class SamlIdpAdminRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(targetEntity=SamlIdpMetadataAdminRoleEntity.class, mappedBy="role")
	private Set<SamlIdpMetadataAdminRoleEntity> adminForIdps;

	public Set<SamlIdpMetadataAdminRoleEntity> getAdminForIdps() {
		return adminForIdps;
	}

	public void setAdminForIdps(Set<SamlIdpMetadataAdminRoleEntity> adminForIdps) {
		this.adminForIdps = adminForIdps;
	}

}
