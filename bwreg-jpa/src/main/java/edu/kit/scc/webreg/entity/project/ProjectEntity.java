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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;

@Entity(name = "ProjectEntity")
@Table(name = "project")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ProjectEntity extends AbstractBaseEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Column(name="name", length=128, nullable=false)
	private String name;
	
	@NotNull
	@Column(name="short_name", length=32, nullable=false, unique=true)
	private String shortName;
	
	@ManyToOne(targetEntity = ProjectEntity.class)
	private ProjectEntity parentProject;
	
	@ManyToOne(targetEntity = ProjectAdminRoleEntity.class)
	private ProjectAdminRoleEntity adminRole;
	
	@Column(name = "description")
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;
	
	@Column(name = "short_description", length = 2048)
	private String shortDescription;

	@Column(name = "sub_projects_allowed")
	private Boolean subProjectsAllowed;
	
	@Enumerated(EnumType.STRING)
	private ProjectStatus projectStatus;	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProjectAdminRoleEntity getAdminRole() {
		return adminRole;
	}

	public void setAdminRole(ProjectAdminRoleEntity adminRole) {
		this.adminRole = adminRole;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public ProjectEntity getParentProject() {
		return parentProject;
	}

	public void setParentProject(ProjectEntity parentProject) {
		this.parentProject = parentProject;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public Boolean getSubProjectsAllowed() {
		return subProjectsAllowed;
	}

	public void setSubProjectsAllowed(Boolean subProjectsAllowed) {
		this.subProjectsAllowed = subProjectsAllowed;
	}

	public ProjectStatus getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(ProjectStatus projectStatus) {
		this.projectStatus = projectStatus;
	}
}
