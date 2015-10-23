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

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import edu.kit.scc.webreg.entity.GlobalGroupEntity;

@Entity(name = "AttributeSourceGroupEntity")
public class AttributeSourceGroupEntity extends GlobalGroupEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = AttributeSourceEntity.class)
	private AttributeSourceEntity attributeSource;

	public AttributeSourceEntity getAttributeSource() {
		return attributeSource;
	}

	public void setAttributeSource(AttributeSourceEntity attributeSource) {
		this.attributeSource = attributeSource;
	}

}
