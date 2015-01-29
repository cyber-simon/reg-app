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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;

@Entity(name = "FederationEntity")
@Table(name = "federation")
public class FederationEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "federation_name", length = 128, unique = true)
	private String name;

	@Column(name = "entity_id", length = 2048)
	private String entityId;

	@Column(name = "federation_metadata_url", length = 2048)
	private String federationMetadataUrl;

	@Column(name = "entity_category_filter", length = 512)
	private String entityCategoryFilter;

	@ManyToOne(targetEntity = BusinessRulePackageEntity.class)
	private BusinessRulePackageEntity entityFilterRulePackage;

	@Column(name = "polled_at")
	private Date polledAt;
	
	@Column(name = "fetch_idps")
	private Boolean fetchIdps;

	@Column(name = "fetch_sps")
	private Boolean fetchSps;

	@Column(name = "fetch_aas")
	private Boolean fetchAAs;
	
	@ManyToMany(targetEntity = SamlIdpMetadataEntity.class, mappedBy="federations")
	private Set<SamlIdpMetadataEntity> idps;

	@ManyToMany(targetEntity = SamlSpMetadataEntity.class, mappedBy="federations")
	private Set<SamlSpMetadataEntity> sps;

	@ManyToMany(targetEntity = SamlAAMetadataEntity.class, mappedBy="federations")
	private Set<SamlAAMetadataEntity> aas;

	@PostLoad
	public void postLoad() {
		// Populate standard values if null, which is the case for updated webreg
		if (fetchIdps == null)
			fetchIdps = Boolean.TRUE;
		if (fetchSps == null)
			fetchSps = Boolean.FALSE;
		if (fetchAAs == null)
			fetchAAs = Boolean.FALSE;
	}
	
	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getFederationMetadataUrl() {
		return federationMetadataUrl;
	}

	public void setFederationMetadataUrl(String federationMetadataUrl) {
		this.federationMetadataUrl = federationMetadataUrl;
	}

	public Date getPolledAt() {
		return polledAt;
	}

	public void setPolledAt(Date polledAt) {
		this.polledAt = polledAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEntityCategoryFilter() {
		return entityCategoryFilter;
	}

	public void setEntityCategoryFilter(String entityCategoryFilter) {
		this.entityCategoryFilter = entityCategoryFilter;
	}

	public Set<SamlIdpMetadataEntity> getIdps() {
		return idps;
	}

	public void setIdps(Set<SamlIdpMetadataEntity> idps) {
		this.idps = idps;
	}

	public BusinessRulePackageEntity getEntityFilterRulePackage() {
		return entityFilterRulePackage;
	}

	public void setEntityFilterRulePackage(
			BusinessRulePackageEntity entityFilterRulePackage) {
		this.entityFilterRulePackage = entityFilterRulePackage;
	}

	public Boolean getFetchIdps() {
		return fetchIdps;
	}

	public void setFetchIdps(Boolean fetchIdps) {
		this.fetchIdps = fetchIdps;
	}

	public Boolean getFetchSps() {
		return fetchSps;
	}

	public void setFetchSps(Boolean fetchSps) {
		this.fetchSps = fetchSps;
	}

	public Boolean getFetchAAs() {
		return fetchAAs;
	}

	public void setFetchAAs(Boolean fetchAAs) {
		this.fetchAAs = fetchAAs;
	}

	public Set<SamlSpMetadataEntity> getSps() {
		return sps;
	}

	public void setSps(Set<SamlSpMetadataEntity> sps) {
		this.sps = sps;
	}

	public Set<SamlAAMetadataEntity> getAas() {
		return aas;
	}

	public void setAas(Set<SamlAAMetadataEntity> aas) {
		this.aas = aas;
	}
}
