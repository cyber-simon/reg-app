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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "SamlIdpMetadataAdminRoleEntity")
@Table(name = "idpmetadata_admin_role")
public class SamlIdpMetadataAdminRoleEntity extends AbstractBaseEntity {
 
	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = SamlIdpMetadataEntity.class)
	private SamlIdpMetadataEntity idp;

	@ManyToOne(targetEntity = SamlIdpAdminRoleEntity.class)
	private SamlIdpAdminRoleEntity role;

	public SamlIdpMetadataEntity getIdp() {
		return idp;
	}

	public void setIdp(SamlIdpMetadataEntity idp) {
		this.idp = idp;
	}

	public SamlIdpAdminRoleEntity getRole() {
		return role;
	}

	public void setRole(SamlIdpAdminRoleEntity role) {
		this.role = role;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((idp == null) ? 0 : idp.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SamlIdpMetadataAdminRoleEntity other = (SamlIdpMetadataAdminRoleEntity) obj;
		if (idp == null) {
			if (other.idp != null)
				return false;
		} else if (!idp.equals(other.idp))
			return false;
		if (role == null) {
			if (other.role != null)
				return false;
		} else if (!role.equals(other.role))
			return false;
		return true;
	}
}
