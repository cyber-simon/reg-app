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
package edu.kit.scc.webreg.dao;

import java.util.List;

import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;

public interface AdminUserDao extends BaseDao<AdminUserEntity, Long> {

	AdminUserEntity findByUsernameAndPassword(String username, String password);

	List<RoleEntity> findRolesForUserById(Long id);

	AdminUserEntity findByUsername(String username);

}
