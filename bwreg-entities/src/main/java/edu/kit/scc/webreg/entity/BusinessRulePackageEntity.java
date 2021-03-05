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

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity(name = "BusinessRulePackageEntity")
@Table(name = "business_rule_package")
public class BusinessRulePackageEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "package_name", length = 512)
	private String packageName;

	@Column(name = "base_name", length = 128)
	@NotNull
	private String knowledgeBaseName;

	@Column(name = "base_version", length = 128)
	@NotNull
	private String knowledgeBaseVersion;

	@OneToMany(mappedBy = "rulePackage", targetEntity = BusinessRuleEntity.class)
	private Set<BusinessRuleEntity> rules;
	
	@Column(name = "dirty_stamp")
	private Date dirtyStamp;

	public String getKnowledgeBaseName() {
		return knowledgeBaseName;
	}

	public void setKnowledgeBaseName(String knowledgeBaseName) {
		this.knowledgeBaseName = knowledgeBaseName;
	}

	public String getKnowledgeBaseVersion() {
		return knowledgeBaseVersion;
	}

	public void setKnowledgeBaseVersion(String knowledgeBaseVersion) {
		this.knowledgeBaseVersion = knowledgeBaseVersion;
	}

	public Set<BusinessRuleEntity> getRules() {
		return rules;
	}

	public void setRules(Set<BusinessRuleEntity> rules) {
		this.rules = rules;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Date getDirtyStamp() {
		return dirtyStamp;
	}

	public void setDirtyStamp(Date dirtyStamp) {
		this.dirtyStamp = dirtyStamp;
	}

}
