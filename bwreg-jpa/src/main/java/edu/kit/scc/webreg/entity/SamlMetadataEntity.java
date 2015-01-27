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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity(name = "SamlMetadataEntity")
@Table(name = "samlmetadata")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class SamlMetadataEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "entity_id", length = 2048)
	private String entityId;
	
	@Enumerated(EnumType.STRING)
	private SamlMetadataEntityStatus status;
	
	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public SamlMetadataEntityStatus getStatus() {
		return status;
	}

	public void setStatus(SamlMetadataEntityStatus status) {
		this.status = status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((entityId == null) ? 0 : entityId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SamlMetadataEntity other = (SamlMetadataEntity) obj;
		if (entityId == null) {
			if (other.entityId != null)
				return false;
		} else if (!entityId.equals(other.entityId))
			return false;
		return true;
	}
}
