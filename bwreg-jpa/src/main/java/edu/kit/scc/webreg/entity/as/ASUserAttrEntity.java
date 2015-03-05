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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Entity(name = "ASUserAttrEntity")
@Table(name = "attribute_src_ua")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ASUserAttrEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;
	
    @ManyToOne
    @JoinColumn(name = "attribute_src_id", nullable = false)
	private AttributeSourceEntity attributeSource;

	@Column(name = "key", nullable = false, length = 512)
	private String key;

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public AttributeSourceEntity getAttributeSource() {
		return attributeSource;
	}

	public void setAttributeSource(AttributeSourceEntity attributeSource) {
		this.attributeSource = attributeSource;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
