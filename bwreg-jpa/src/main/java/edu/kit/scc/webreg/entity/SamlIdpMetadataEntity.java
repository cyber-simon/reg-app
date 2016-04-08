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

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "SamlIdpMetadataEntity")
@Table(name = "idpmetadata")
public class SamlIdpMetadataEntity extends SamlMetadataEntity {

	private static final long serialVersionUID = 1L;

	@ManyToMany(targetEntity = FederationEntity.class,  
			cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(name = "idpmetadata_federation",
			joinColumns = @JoinColumn(name = "idpmetadata_id"),
			inverseJoinColumns = @JoinColumn(name = "federation_id"))			
	private Set<FederationEntity> federations;
	
	@OneToMany(targetEntity = SamlIdpScopeEntity.class, 
			mappedBy = "idp", cascade = CascadeType.REMOVE)
	private Set<SamlIdpScopeEntity> scopes; 
	
	@ElementCollection
	@JoinTable(name = "idp_entity_categories")
	@Column(name = "value_data", length = 2048)
	private List<String> entityCategoryList;
	
	@Enumerated(EnumType.STRING)
	private SamlIdpMetadataEntityStatus aqIdpStatus;
	
	@Column(name = "last_aq_status_change")
	private Date lastAqStatusChange;

	@Enumerated(EnumType.STRING)
	private SamlIdpMetadataEntityStatus idIdpStatus;
	
	@Column(name = "last_id_status_change")
	private Date lastIdStatusChange;
	
	public Set<FederationEntity> getFederations() {
		return federations;
	}

	public void setFederations(Set<FederationEntity> federations) {
		this.federations = federations;
	}

	public Set<SamlIdpScopeEntity> getScopes() {
		return scopes;
	}

	public void setScopes(Set<SamlIdpScopeEntity> scopes) {
		this.scopes = scopes;
	}
	
	public List<String> getEntityCategoryList() {
		return entityCategoryList;
	}

	public void setEntityCategoryList(List<String> entityCategoryList) {
		this.entityCategoryList = entityCategoryList;
	}

	public SamlIdpMetadataEntityStatus getAqIdpStatus() {
		return aqIdpStatus;
	}

	public void setAqIdpStatus(SamlIdpMetadataEntityStatus aqIdpStatus) {
		this.aqIdpStatus = aqIdpStatus;
	}

	public Date getLastAqStatusChange() {
		return lastAqStatusChange;
	}

	public void setLastAqStatusChange(Date lastAqStatusChange) {
		this.lastAqStatusChange = lastAqStatusChange;
	}

	public SamlIdpMetadataEntityStatus getIdIdpStatus() {
		return idIdpStatus;
	}

	public void setIdIdpStatus(SamlIdpMetadataEntityStatus idIdpStatus) {
		this.idIdpStatus = idIdpStatus;
	}

	public Date getLastIdStatusChange() {
		return lastIdStatusChange;
	}

	public void setLastIdStatusChange(Date lastIdStatusChange) {
		this.lastIdStatusChange = lastIdStatusChange;
	}
}
