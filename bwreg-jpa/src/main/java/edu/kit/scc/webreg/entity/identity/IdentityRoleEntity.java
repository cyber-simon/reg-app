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

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.RoleEntity;

@Entity(name = "IdentityRoleEntity")
@Table(name = "idty_role")
public class IdentityRoleEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
	@ManyToOne(targetEntity = IdentityEntity.class)
    @JoinColumn(name = "identity_id", nullable = false)
	private IdentityEntity identity;
	
    @Id
	@ManyToOne(targetEntity = RoleEntity.class)
    @JoinColumn(name = "role_id", nullable = false)
	private RoleEntity role;

	public RoleEntity getRole() {
		return role;
	}

	public void setRole(RoleEntity role) {
		this.role = role;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdentityRoleEntity other = (IdentityRoleEntity) obj;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		return true;
	}	
}
