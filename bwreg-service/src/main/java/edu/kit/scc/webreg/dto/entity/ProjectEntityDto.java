package edu.kit.scc.webreg.dto.entity;

import java.util.Set;

import edu.kit.scc.webreg.entity.project.ProjectStatus;

public class ProjectEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	private String name;
	
	private String shortName;
	
	private String groupName;
	
	private Long parentProjectId;
	
	private Set<Long> childProjects;

	private String description;
	
	private String shortDescription;

	private Boolean subProjectsAllowed;

	private Boolean published;
	
	private Boolean approved;

	private String attributePrefix;
	
	private String attributeName;
	
	private ProjectStatus projectStatus;

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

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Long getParentProjectId() {
		return parentProjectId;
	}

	public void setParentProjectId(Long parentProjectId) {
		this.parentProjectId = parentProjectId;
	}

	public Set<Long> getChildProjects() {
		return childProjects;
	}

	public void setChildProjects(Set<Long> childProjects) {
		this.childProjects = childProjects;
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

	public ProjectStatus getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(ProjectStatus projectStatus) {
		this.projectStatus = projectStatus;
	}	
	
}
