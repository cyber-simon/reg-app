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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

	@OneToMany(targetEntity = SamlIdpMetadataAdminRoleEntity.class, 
			mappedBy = "idp")
	private Set<SamlIdpMetadataAdminRoleEntity> adminRoles; 

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

	public Set<SamlIdpMetadataAdminRoleEntity> getAdminRoles() {
		return adminRoles;
	}

	public void setAdminRoles(Set<SamlIdpMetadataAdminRoleEntity> adminRoles) {
		this.adminRoles = adminRoles;
	}
}
