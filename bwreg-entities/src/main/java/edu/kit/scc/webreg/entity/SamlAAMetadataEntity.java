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

import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity(name = "SamlAAMetadataEntity")
@Table(name = "aametadata")
public class SamlAAMetadataEntity extends SamlMetadataEntity {

	private static final long serialVersionUID = 1L;

	@ManyToMany(targetEntity = FederationEntity.class,  
			cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(name = "aametadata_federation",
			joinColumns = @JoinColumn(name = "aametadata_id"),
			inverseJoinColumns = @JoinColumn(name = "federation_id"))			
	private Set<FederationEntity> federations;
	
	@ElementCollection
	@JoinTable(name = "aametadata_entity_categories")
	@Column(name = "value_data", length = 2048)
	private List<String> entityCategoryList;
	
	public Set<FederationEntity> getFederations() {
		return federations;
	}

	public void setFederations(Set<FederationEntity> federations) {
		this.federations = federations;
	}

	public List<String> getEntityCategoryList() {
		return entityCategoryList;
	}

	public void setEntityCategoryList(List<String> entityCategoryList) {
		this.entityCategoryList = entityCategoryList;
	}
}
