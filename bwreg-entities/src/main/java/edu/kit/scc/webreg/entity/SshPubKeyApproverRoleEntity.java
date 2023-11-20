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

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity(name = "SshPubKeyApproverRoleEntity")
public class SshPubKeyApproverRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(targetEntity = ServiceEntity.class, mappedBy = "sshPubKeyApproverRole")
	private Set<ServiceEntity> sshPubKeyApproverForServices;

	public Set<ServiceEntity> getSshPubKeyApproverForServices() {
		return sshPubKeyApproverForServices;
	}

	public void setSshPubKeyApproverForServices(Set<ServiceEntity> sshPubKeyApproverForServices) {
		this.sshPubKeyApproverForServices = sshPubKeyApproverForServices;
	}	
}
