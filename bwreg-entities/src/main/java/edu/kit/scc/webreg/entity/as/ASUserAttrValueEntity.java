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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "ASUserAttrValueEntity")
@Table(name = "attribute_src_ua_value")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ASUserAttrValueEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = ASUserAttrEntity.class)
    @JoinColumn(name = "attribute_src_id", nullable = false)
	private ASUserAttrEntity asUserAttr;

	@Column(name = "as_key", nullable = false, length = 512)
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public ASUserAttrEntity getAsUserAttr() {
		return asUserAttr;
	}

	public void setAsUserAttr(ASUserAttrEntity asUserAttr) {
		this.asUserAttr = asUserAttr;
	}
}
