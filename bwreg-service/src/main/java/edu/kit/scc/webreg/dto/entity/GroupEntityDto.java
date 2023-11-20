package edu.kit.scc.webreg.dto.entity;

import java.util.Set;

import jakarta.validation.constraints.NotNull;

public class GroupEntityDto extends AbstractBaseEntityDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	private String name;

	private Integer gidNumber;

	private Set<GroupEntityDto> parents;
	
	private Set<UserEntityDto> users;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getGidNumber() {
		return gidNumber;
	}

	public void setGidNumber(Integer gidNumber) {
		this.gidNumber = gidNumber;
	}

	public Set<GroupEntityDto> getParents() {
		return parents;
	}

	public void setParents(Set<GroupEntityDto> parents) {
		this.parents = parents;
	}

	public Set<UserEntityDto> getUsers() {
		return users;
	}

	public void setUsers(Set<UserEntityDto> users) {
		this.users = users;
	}
}
