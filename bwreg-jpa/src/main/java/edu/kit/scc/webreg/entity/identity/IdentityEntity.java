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
import javax.validation.constraints.NotNull;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "IdentityEntity")
@Table(name = "idty")
public class IdentityEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name="name", length=128, nullable=false, unique=true)
	private String name;
	
	@OneToMany(targetEntity=IdentityEntity.class, mappedBy = "identity")
	private Set<IdentityEntity> identities;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<IdentityEntity> getIdentities() {
		return identities;
	}

	public void setIdentities(Set<IdentityEntity> identities) {
		this.identities = identities;
	}
}
