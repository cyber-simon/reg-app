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

import edu.kit.scc.webreg.entity.project.ProjectPolicyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "PolicyEntity")
@Table(name = "policy")
public class PolicyEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(targetEntity = ServiceEntity.class)
	private ServiceEntity service;
	
	@ManyToOne(targetEntity = ServiceEntity.class)
    @JoinColumn(name = "project_policy_id")
	private ServiceEntity projectPolicy;

	@Enumerated(EnumType.STRING)
	private ProjectPolicyType projectPolicyType;	

	@OneToMany(targetEntity = AgreementTextEntity.class, mappedBy="policy")
	private Set<AgreementTextEntity> agreementTexts;

	@OneToOne(targetEntity = AgreementTextEntity.class)
	private AgreementTextEntity actualAgreement;
	
	@Column(name = "show_only")
	private Boolean showOnly;
	
	@Column(name = "hidden")
	private Boolean hidden;
	
	@Column(name = "name", length = 128)
	private String name;
	
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

	public Boolean getShowOnly() {
		return showOnly;
	}

	public void setShowOnly(Boolean showOnly) {
		this.showOnly = showOnly;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public ServiceEntity getProjectPolicy() {
		return projectPolicy;
	}

	public void setProjectPolicy(ServiceEntity projectPolicy) {
		this.projectPolicy = projectPolicy;
	}

	public ProjectPolicyType getProjectPolicyType() {
		return projectPolicyType;
	}

	public void setProjectPolicyType(ProjectPolicyType projectPolicyType) {
		this.projectPolicyType = projectPolicyType;
	}

}
