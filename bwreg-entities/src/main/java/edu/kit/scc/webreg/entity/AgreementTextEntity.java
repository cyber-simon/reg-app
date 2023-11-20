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

import java.sql.Types;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity(name = "AgreementTextEntity")
@Table(name = "agreement_text")
public class AgreementTextEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;
 
	@Column(name = "agreement")
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)
	private String agreement;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@JoinTable(name = "agreement_text_i18")
    @MapKeyColumn(name = "key_data", length = 128)
    @Column(name = "value_data")
    @Lob
	@JdbcTypeCode(Types.LONGVARCHAR)
    private Map<String, String> agreementMap;
	
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

	public Map<String, String> getAgreementMap() {
		return agreementMap;
	}

	public void setAgreementMap(Map<String, String> agreementMap) {
		this.agreementMap = agreementMap;
	}
}
