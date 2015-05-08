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
import java.util.Set;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.HomeOrgGroupDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public interface GroupServiceHook {

	void setAppConfig(ApplicationConfig appConfig);
	
	HomeOrgGroupEntity preUpdateUserPrimaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, HomeOrgGroupEntity group, UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) 
			throws UserUpdateException;

	HomeOrgGroupEntity postUpdateUserPrimaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, HomeOrgGroupEntity group, UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) 
			throws UserUpdateException;

	void preUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) 
			throws UserUpdateException;

	void postUpdateUserSecondaryGroupFromAttribute(HomeOrgGroupDao dao, GroupDao groupDao, UserEntity user, Map<String, List<Object>> attributeMap, Auditor auditor, Set<GroupEntity> changedGroups) 
			throws UserUpdateException;

	boolean isPrimaryResponsible(UserEntity user, Map<String, List<Object>> attributeMap);

	boolean isPrimaryCompleteOverride();

	boolean isSecondaryResponsible(UserEntity user, Map<String, List<Object>> attributeMap);

	boolean isSecondaryCompleteOverride();
	
}
