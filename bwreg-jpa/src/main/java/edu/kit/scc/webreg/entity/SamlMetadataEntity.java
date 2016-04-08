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

import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity(name = "SamlMetadataEntity")
@Table(name = "samlmetadata")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SamlMetadataEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "entity_id", length = 2048)
	private String entityId;
	
	@Enumerated(EnumType.STRING)
	private SamlMetadataEntityStatus status;

	@Column(name = "org_name", length = 512)
	private String orgName;
	
	@Column(name = "display_name", length = 512)
	private String displayName;
	
	@Column(name = "description", length = 1024)
	private String description;
	
	@Column(name = "information_url", length = 1024)
	private String informationUrl;
	
	@Column(name = "entity_desc")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String entityDescriptor;
	
	@ElementCollection
	@JoinTable(name = "samlmetadata_generic_store")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 
	
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
		SamlMetadataEntity other = (SamlMetadataEntity) obj;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		return true;
	}
}
