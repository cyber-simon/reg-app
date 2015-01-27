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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "PolicyEntity")
@Table(name = "policy")
public class PolicyEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity service;
	
	@OneToMany(targetEntity = AgreementTextEntity.class, mappedBy="policy")
	private Set<AgreementTextEntity> agreementTexts;

	@OneToOne(targetEntity = AgreementTextEntity.class)
	private AgreementTextEntity actualAgreement;
	
	@Column(name = "mandatory")
	private Boolean mandatory;
	
	@Column(name = "name", length = 128)
	private String name;
	
	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setSevice(ServiceEntity service) {
		this.service = service;
	}

	public Set<AgreementTextEntity> getAgreementTexts() {
		return agreementTexts;
	}

	public void setAgreementTexts(Set<AgreementTextEntity> agreementTexts) {
		this.agreementTexts = agreementTexts;
	}

	public AgreementTextEntity getActualAgreement() {
		return actualAgreement;
	}

	public void setActualAgreement(AgreementTextEntity actualAgreement) {
		this.actualAgreement = actualAgreement;
	}

}
