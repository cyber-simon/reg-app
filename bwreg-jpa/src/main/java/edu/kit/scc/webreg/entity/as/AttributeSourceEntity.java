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

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
	
	@Column(name = "service_source")
	private Boolean serviceSource;
	
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
}
