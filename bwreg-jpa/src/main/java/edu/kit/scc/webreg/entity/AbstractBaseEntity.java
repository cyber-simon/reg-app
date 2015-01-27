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

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@MappedSuperclass
public abstract class AbstractBaseEntity implements BaseEntity<Long>, Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	protected Long id;
	
	@Column(name = "created_at")
	protected Date createdAt;

	@Column(name = "updated_at")
	protected Date updatedAt;
	
	@Column(name = "version")
	protected Integer version;
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object other) {
		if (other == null) return false;
		
	    return this.getClass().equals(other.getClass()) && 
	    		(getId() != null) 
	         ? getId().equals(((BaseEntity<Long>) other).getId()) 
	         : (other == this);
	}

	public int hashCode() {
	    return (getId() != null) 
	         ? (getClass().hashCode() + getId().hashCode())
	         : super.hashCode();
	}

	@PrePersist
	public void prePersist() {
		setVersion(new Integer(0));
		Date d = new Date();
		setCreatedAt(d);
		setUpdatedAt(d);
	}

	@PreUpdate
	public void preUpdate() {
		if (getVersion() == null)
			setVersion(new Integer(0));
		
		setVersion(getVersion() + 1);
		setUpdatedAt(new Date());
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}		
}
