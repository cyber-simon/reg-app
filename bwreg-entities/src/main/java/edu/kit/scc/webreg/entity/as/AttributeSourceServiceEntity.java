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

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Entity(name = "AttributeSourceServiceEntity")
@Table(name = "attribute_src_service")
public class AttributeSourceServiceEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
    @JoinColumn(name = "service_id", nullable = false)
	private ServiceEntity service;
	
    @ManyToOne(targetEntity = AttributeSourceEntity.class)
    @JoinColumn(name = "attribute_src_id", nullable = false)
	private AttributeSourceEntity attributeSource;

	public AttributeSourceEntity getAttributeSource() {
		return attributeSource;
	}

	public void setAttributeSource(AttributeSourceEntity attributeSource) {
		this.attributeSource = attributeSource;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

}
