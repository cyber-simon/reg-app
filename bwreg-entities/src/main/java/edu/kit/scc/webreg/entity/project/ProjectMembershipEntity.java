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
package edu.kit.scc.webreg.entity.project;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@Entity(name = "ProjectMembershipEntity")
@Table(name = "project_idty_membership")
public class ProjectMembershipEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
	@ManyToOne(targetEntity = IdentityEntity.class)
    @JoinColumn(name = "identity_id", nullable = false)
	private IdentityEntity identity;
	
    @Id
    @ManyToOne(targetEntity = ProjectEntity.class)
    @JoinColumn(name = "project_id", nullable = false)
	private ProjectEntity project;

	@Enumerated(EnumType.STRING)
	private ProjectMembershipType membershipType;	

	public ProjectEntity getProject() {
		return project;
	}

	public void setProject(ProjectEntity project) {
		this.project = project;
	}

	public ProjectMembershipType getMembershipType() {
		return membershipType;
	}

	public void setMembershipType(ProjectMembershipType membershipType) {
		this.membershipType = membershipType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((identity == null) ? 0 : identity.hashCode());
		result = prime * result + ((membershipType == null) ? 0 : membershipType.hashCode());
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectMembershipEntity other = (ProjectMembershipEntity) obj;
		if (identity == null) {
			if (other.identity != null)
				return false;
		} else if (!identity.equals(other.identity))
			return false;
		if (membershipType != other.membershipType)
			return false;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		return true;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityEntity identity) {
		this.identity = identity;
	}	
}
