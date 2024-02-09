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

import java.sql.Types;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;

import edu.kit.scc.webreg.entity.attribute.AttributeConsumerEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity(name = "SamlSpMetadataEntity")
@Table(name = "spmetadata")
public class SamlSpMetadataEntity extends AttributeConsumerEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "entity_id", length = 4096)
	private String entityId;
	
	@Enumerated(EnumType.STRING)
	private SamlMetadataEntityStatus status;

	@Column(name = "org_name", length = 4096)
	private String orgName;
	
	@Column(name = "display_name", length = 4096)
	private String displayName;
	
	@Column(name = "description", length = 4096)
	private String description;
	
	@Column(name = "information_url", length = 4096)
	private String informationUrl;
	
	@Column(name = "logo_url", length = 2097152)
	private String logoUrl;
	
	@Column(name = "logo_small_url", length = 2097152)
	private String logoSmallUrl;
	
	@Column(name = "entity_desc", columnDefinition="TEXT")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)	
	private String entityDescriptor;
	
	@ElementCollection
	@JoinTable(name = "spmetadata_generic_store")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 

	@ManyToMany(targetEntity = FederationEntity.class,  
			cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
	@JoinTable(name = "spmetadata_federation",
			joinColumns = @JoinColumn(name = "spmetadata_id"),
			inverseJoinColumns = @JoinColumn(name = "federation_id"))			
	private Set<FederationEntity> federations;

	private Boolean managedInteral;
	
	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getEntityDescriptor() {
		return entityDescriptor;
	}

	public void setEntityDescriptor(String entityDescriptor) {
		this.entityDescriptor = entityDescriptor;
	}

	public SamlMetadataEntityStatus getStatus() {
		return status;
	}

	public void setStatus(SamlMetadataEntityStatus status) {
		this.status = status;
	}

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getInformationUrl() {
		return informationUrl;
	}

	public void setInformationUrl(String informationUrl) {
		this.informationUrl = informationUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((entityId == null) ? 0 : entityId.hashCode());
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
		SamlSpMetadataEntity other = (SamlSpMetadataEntity) obj;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		return true;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getLogoSmallUrl() {
		return logoSmallUrl;
	}

	public void setLogoSmallUrl(String logoSmallUrl) {
		this.logoSmallUrl = logoSmallUrl;
	}
	
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
