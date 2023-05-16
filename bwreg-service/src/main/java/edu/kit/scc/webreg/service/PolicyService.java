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
package edu.kit.scc.webreg.service;

import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectPolicyType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;

public interface PolicyService extends BaseService<PolicyEntity> {

	PolicyEntity findWithAgreemets(Long id);
	
	List<PolicyEntity> resolvePoliciesForService(ServiceEntity service, UserEntity user);

	Map<ProjectServiceEntity, List<PolicyEntity>> findPolicyMapForProject(ProjectEntity project,
			ProjectPolicyType policyType);
	
}
