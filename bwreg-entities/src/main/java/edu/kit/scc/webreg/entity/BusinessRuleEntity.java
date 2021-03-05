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

@Entity(name = "BusinessRuleEntity")
@Table(name = "business_rule")
public class BusinessRuleEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "rule_text")
	@Lob
	@Type(type = "org.hibernate.type.TextType")	
	private String rule;
	
	@Column(name = "rule_type", length = 32)
	private String ruleType;
	
	@Column(name = "name", length = 128)
	private String name;
	
	@Column(name = "base_name", length = 128)
	private String knowledgeBaseName;

	@ManyToOne(targetEntity = BusinessRulePackageEntity.class)
	private BusinessRulePackageEntity rulePackage;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getKnowledgeBaseName() {
		return knowledgeBaseName;
	}

	public void setKnowledgeBaseName(String knowledgeBaseName) {
		this.knowledgeBaseName = knowledgeBaseName;
	}

	public BusinessRulePackageEntity getRulePackage() {
		return rulePackage;
	}

	public void setRulePackage(BusinessRulePackageEntity rulePackage) {
		this.rulePackage = rulePackage;
	}
}
