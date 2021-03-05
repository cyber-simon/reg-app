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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity(name = "AgreementTextEntity")
@Table(name = "agreement_text")
public class AgreementTextEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
 
	@Column(name = "agreement")
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String agreement;
	
	@Column(name = "name", length = 128)
	private String name;
	
	@ManyToOne(targetEntity = PolicyEntity.class)
	private PolicyEntity policy;
	
	public String getAgreement() {
		return agreement;
	}

	public void setAgreement(String agreement) {
		this.agreement = agreement;
	}

	public PolicyEntity getPolicy() {
		return policy;
	}

	public void setPolicy(PolicyEntity policy) {
		this.policy = policy;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
