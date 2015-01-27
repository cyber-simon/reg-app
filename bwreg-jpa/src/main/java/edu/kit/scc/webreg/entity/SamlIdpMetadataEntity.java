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
import java.util.Map;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

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

	@Column(name = "entity_desc")
	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String entityDescriptor;

	@Column(name = "org_name", length = 512)
	private String orgName;
	
	@Column(name = "display_name", length = 512)
	private String displayName;
	
	@Column(name = "description", length = 1024)
	private String description;
	
	@Column(name = "information_url", length = 1024)
	private String informationUrl;
	
	@OneToMany(targetEntity = SamlIdpScopeEntity.class, 
			mappedBy = "idp", cascade = CascadeType.REMOVE)
	private Set<SamlIdpScopeEntity> scopes; 
	
	@ElementCollection
	@JoinTable(name = "idp_generic_store")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> genericStore; 
	
	@ElementCollection
	@JoinTable(name = "idp_entity_categories")
	@Column(name = "value_data", length = 2048)
	private List<String> entityCategoryList;
	
	public String getEntityDescriptor() {
		return entityDescriptor;
	}

	public void setEntityDescriptor(String entityDescriptor) {
		this.entityDescriptor = entityDescriptor;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Set<SamlIdpScopeEntity> getScopes() {
		return scopes;
	}

	public void setScopes(Set<SamlIdpScopeEntity> scopes) {
		this.scopes = scopes;
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

	public Map<String, String> getGenericStore() {
		return genericStore;
	}

	public void setGenericStore(Map<String, String> genericStore) {
		this.genericStore = genericStore;
	}

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
