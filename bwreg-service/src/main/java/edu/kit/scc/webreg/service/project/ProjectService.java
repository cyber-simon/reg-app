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
package edu.kit.scc.webreg.service.project;

import java.util.List;
import java.util.Set;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.service.BaseService;

public interface ProjectService extends BaseService<ProjectEntity> {

	void updateProjectMemberList(ProjectEntity project, Set<IdentityEntity> memberList, String executor);
	void updateServices(ProjectEntity project, Set<ServiceEntity> services, String executor);
	void addAdminToProject(ProjectEntity project, IdentityEntity identity, ProjectAdminType type, String executor);
	void removeAdminFromProject(ProjectIdentityAdminEntity pia, String executor);
	
	List<ProjectEntity> findByService(ServiceEntity service);

	ProjectEntity save(ProjectEntity project, Long identityId);

	List<ProjectIdentityAdminEntity> findAdminByUserId(Long identityId);

	List<ProjectMembershipEntity> findMembersForProject(ProjectEntity project);
	
	List<ProjectIdentityAdminEntity> findAdminsForProject(ProjectEntity project);
	
	List<ProjectServiceEntity> findServicesForProject(ProjectEntity project);
	
	List<ProjectMembershipEntity> findByIdentity(IdentityEntity identity);
}
