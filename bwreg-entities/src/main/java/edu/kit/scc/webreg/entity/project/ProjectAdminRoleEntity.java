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

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import edu.kit.scc.webreg.entity.RoleEntity;

@Entity(name = "ProjectAdminRoleEntity")
public class ProjectAdminRoleEntity extends RoleEntity {

	private static final long serialVersionUID = 1L;

	@OneToMany(targetEntity=ProjectEntity.class, mappedBy="adminRole")
	private Set<ProjectEntity> adminForProjects;

	public Set<ProjectEntity> getAdminForProjects() {
		return adminForProjects;
	}

	public void setAdminForProjects(Set<ProjectEntity> adminForProjects) {
		this.adminForProjects = adminForProjects;
	}

}
