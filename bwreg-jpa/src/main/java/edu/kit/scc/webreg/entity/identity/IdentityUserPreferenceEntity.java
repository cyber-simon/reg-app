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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "IdentityRoleEntity")
@Table(name = "idty_role")
public class IdentityUserPreferenceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
	@ManyToOne(targetEntity = IdentityEntity.class)
    @JoinColumn(name = "identity_id", nullable = false)
	private IdentityEntity identity;
	
    @Id
	@Column(name="pref_type", length=64)
    private String prefType;

	@ManyToOne(targetEntity = UserEntity.class)
    @JoinColumn(name = "user_id")
	private UserEntity user;

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public String getPrefType() {
		return prefType;
	}

	public void setPrefType(String prefType) {
		this.prefType = prefType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		result = prime * result + ((prefType == null) ? 0 : prefType.hashCode());
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
		IdentityUserPreferenceEntity other = (IdentityUserPreferenceEntity) obj;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		if (prefType == null) {
			if (other.prefType != null)
				return false;
		} else if (!prefType.equals(other.prefType))
			return false;
		return true;
	}

}
