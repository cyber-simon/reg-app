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

import java.sql.Types;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;

import edu.kit.scc.webreg.entity.AbstractBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

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
	@Pattern(regexp = "^[a-z]{1}[a-z0-9-_]{0,31}$")	
	private String shortName;
	
	@NotNull
	@Column(name="group_name", length=64, nullable=false, unique=true)
	@Pattern(regexp = "^[a-z]{1}[a-z0-9-_]{0,63}$")
	private String groupName;
	
	@ManyToOne(targetEntity = ProjectEntity.class)
	private ProjectEntity parentProject;
	
	@OneToMany(mappedBy = "parentProject")
	private Set<ProjectEntity> childProjects;

	@Column(name = "description")
	@Lob
	@JdbcTypeCode(Types.LONGVARCHAR)
	private String description;
	
	@Column(name = "short_description", length = 2048)
	private String shortDescription;

	@Column(name = "sub_projects_allowed")
	private Boolean subProjectsAllowed;

	@Column(name = "published")
	private Boolean published;
	
	@Column(name = "approved")
	private Boolean approved;

	@Column(name = "attribute_prefix", length = 2048)
	private String attributePrefix;
	
	@Column(name = "attribute_name", length = 256)
	private String attributeName;
	
	@Enumerated(EnumType.STRING)
	private ProjectStatus projectStatus;	

	@NotNull
	@OneToOne(targetEntity = LocalProjectGroupEntity.class)
    @JoinColumn(name = "group_id", nullable = false)
	private LocalProjectGroupEntity projectGroup;
	
	@OneToMany(mappedBy = "project")
	private Set<ProjectServiceEntity> projectServices;

	@OneToMany(mappedBy = "project")
	private Set<ProjectIdentityAdminEntity> projectAdmins;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public LocalProjectGroupEntity getProjectGroup() {
		return projectGroup;
	}

	public void setProjectGroup(LocalProjectGroupEntity projectGroup) {
		this.projectGroup = projectGroup;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Set<ProjectServiceEntity> getProjectServices() {
		return projectServices;
	}

	public void setProjectServices(Set<ProjectServiceEntity> projectServices) {
		this.projectServices = projectServices;
	}

	public Set<ProjectEntity> getChildProjects() {
		return childProjects;
	}

	public void setChildProjects(Set<ProjectEntity> childProjects) {
		this.childProjects = childProjects;
	}

	public Set<ProjectIdentityAdminEntity> getProjectAdmins() {
		return projectAdmins;
	}

	public void setProjectAdmins(Set<ProjectIdentityAdminEntity> projectAdmins) {
		this.projectAdmins = projectAdmins;
	}

	public Boolean getPublished() {
		return published;
	}

	public void setPublished(Boolean published) {
		this.published = published;
	}

	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	public String getAttributePrefix() {
		return attributePrefix;
	}

	public void setAttributePrefix(String attributePrefix) {
		this.attributePrefix = attributePrefix;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
}
