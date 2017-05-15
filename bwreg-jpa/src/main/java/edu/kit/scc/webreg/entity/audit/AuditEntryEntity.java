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
package edu.kit.scc.webreg.entity.audit;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name  = "AuditEntryEntity")
@Table(name = "audit_entry")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AuditEntryEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "start_time")
	private Date startTime;
	
	@Column(name = "end_time")
	private Date endTime;
	
	@Column(name = "audit_name", length=256)
	private String name;
	
	@Column(name = "audit_detail", length=1024)
	private String detail;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE },
			targetEntity = AuditDetailEntity.class, mappedBy = "auditEntry")
	private Set<AuditDetailEntity> auditDetails;

	@Column(name = "audit_executor", length=64)
	private String executor;

	@ManyToOne(targetEntity = AuditEntryEntity.class)
	private AuditEntryEntity parentEntry;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE },
			targetEntity = AuditEntryEntity.class, mappedBy = "parentEntry")
	private Set<AuditEntryEntity> childEntries;

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Set<AuditDetailEntity> getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(Set<AuditDetailEntity> auditDetails) {
		this.auditDetails = auditDetails;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getExecutor() {
		return executor;
	}

	public void setExecutor(String executor) {
		this.executor = executor;
	}

	public AuditEntryEntity getParentEntry() {
		return parentEntry;
	}

	public void setParentEntry(AuditEntryEntity parentEntry) {
		this.parentEntry = parentEntry;
	}

	public Set<AuditEntryEntity> getChildEntries() {
		return childEntries;
	}

	public void setChildEntries(Set<AuditEntryEntity> childEntries) {
		this.childEntries = childEntries;
	}


}
