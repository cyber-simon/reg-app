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
package edu.kit.scc.webreg.entity.as;

import java.util.Map;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "AttributeSourceEntity")
@Table(name = "attribute_src")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AttributeSourceEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@ElementCollection(fetch = FetchType.LAZY)
    @Fetch(value = FetchMode.SELECT)
	@JoinTable(name = "attribute_src_properties")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data", length = 2048)
    private Map<String, String> asProps; 

	@Column(name = "as_class", length=256, nullable=false)
	private String asClass;
	
	@Column(name = "user_source")
	private Boolean userSource;
	
	@Column(name = "identity_source")
	private Boolean identitySource;
	
	@Column(name = "service_source")
	private Boolean serviceSource;

	@Column(name = "project_source")
	private Boolean projectSource;
	
	@OneToMany(targetEntity = AttributeSourceServiceEntity.class, mappedBy="attributeSource")
	private Set<AttributeSourceServiceEntity> attributeSourceServices;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getAsProps() {
		return asProps;
	}

	public void setAsProps(Map<String, String> asProps) {
		this.asProps = asProps;
	}

	public String getAsClass() {
		return asClass;
	}

	public void setAsClass(String asClass) {
		this.asClass = asClass;
	}

	public Boolean getUserSource() {
		return userSource;
	}

	public void setUserSource(Boolean userSource) {
		this.userSource = userSource;
	}

	public Boolean getServiceSource() {
		return serviceSource;
	}

	public void setServiceSource(Boolean serviceSource) {
		this.serviceSource = serviceSource;
	}

	public Set<AttributeSourceServiceEntity> getAttributeSourceServices() {
		return attributeSourceServices;
	}

	public void setAttributeSourceServices(
			Set<AttributeSourceServiceEntity> attributeSourceServices) {
		this.attributeSourceServices = attributeSourceServices;
	}

	public Boolean getIdentitySource() {
		return identitySource;
	}

	public void setIdentitySource(Boolean identitySource) {
		this.identitySource = identitySource;
	}

	public Boolean getProjectSource() {
		return projectSource;
	}

	public void setProjectSource(Boolean projectSource) {
		this.projectSource = projectSource;
	}
}
