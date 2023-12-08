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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Entity(name = "ProjectServiceEntity")
@Table(name = "project_service")
public class ProjectServiceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
	@ManyToOne(targetEntity = ServiceEntity.class)
    @JoinColumn(name = "service_id", nullable = false)
	private ServiceEntity service;
	
    @Id
    @ManyToOne(targetEntity = ProjectEntity.class)
    @JoinColumn(name = "project_id", nullable = false)
	private ProjectEntity project;

	@Enumerated(EnumType.STRING)
	private ProjectServiceType type;	

	@Enumerated(EnumType.STRING)
	private ProjectServiceStatusType status;

	@Column(name = "group_name_override", length = 64)
	@Pattern(regexp = "^[a-z]{1}[a-z0-9-_]{0,63}$")
	private String groupNameOverride;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((project == null) ? 0 : project.hashCode());
		result = prime * result + ((service == null) ? 0 : service.hashCode());
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
		ProjectServiceEntity other = (ProjectServiceEntity) obj;
		if (project == null) {
			if (other.project != null)
				return false;
		} else if (!project.equals(other.project))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}

	public ProjectEntity getProject() {
		return project;
	}

	public void setProject(ProjectEntity project) {
		this.project = project;
	}

	public ServiceEntity getService() {
		return service;
	}

	public void setService(ServiceEntity service) {
		this.service = service;
	}

	public ProjectServiceType getType() {
		return type;
	}

	public void setType(ProjectServiceType type) {
		this.type = type;
	}

	public ProjectServiceStatusType getStatus() {
		return status;
	}

	public void setStatus(ProjectServiceStatusType status) {
		this.status = status;
	}

	public String getGroupNameOverride() {
		return groupNameOverride;
	}

	public void setGroupNameOverride(String groupNameOverride) {
		this.groupNameOverride = groupNameOverride;
	}
}
