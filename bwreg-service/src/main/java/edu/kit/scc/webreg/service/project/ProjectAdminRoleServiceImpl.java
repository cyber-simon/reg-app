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

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.project.ProjectAdminRoleDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminRoleEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class ProjectAdminRoleServiceImpl extends BaseServiceImpl<ProjectAdminRoleEntity, Long> implements ProjectAdminRoleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ProjectAdminRoleDao dao;
	
	@Override
	public List<ProjectAdminRoleEntity> findWithServices(UserEntity user) {
		return dao.findWithServices(user);
	}
	
	@Override
	public ProjectAdminRoleEntity findWithUsers(Long id) {
		return dao.findWithUsers(id);
	}
	
	@Override
	public ProjectAdminRoleEntity findByName(String name) {
		return dao.findByName(name);
	}

	@Override
	protected BaseDao<ProjectAdminRoleEntity, Long> getDao() {
		return dao;
	}
}
