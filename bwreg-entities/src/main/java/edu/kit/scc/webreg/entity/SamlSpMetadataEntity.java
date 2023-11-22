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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity(name = "SamlSpMetadataEntity")
@Table(name = "spmetadata")
public class SamlSpMetadataEntity extends SamlMetadataEntity {

	private static final long serialVersionUID = 1L;

	@ManyToMany(targetEntity = FederationEntity.class,  
			cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(name = "spmetadata_federation",
			joinColumns = @JoinColumn(name = "spmetadata_id"),
			inverseJoinColumns = @JoinColumn(name = "federation_id"))			
	private Set<FederationEntity> federations;

	private Boolean managedInteral;
	
	public Set<FederationEntity> getFederations() {
		return federations;
	}

	public void setFederations(Set<FederationEntity> federations) {
		this.federations = federations;
	}

	public Boolean getManagedInteral() {
		return managedInteral;
	}

	public void setManagedInteral(Boolean managedInteral) {
		this.managedInteral = managedInteral;
	}
	
}
