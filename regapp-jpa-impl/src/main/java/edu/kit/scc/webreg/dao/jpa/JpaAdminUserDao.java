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
package edu.kit.scc.webreg.dao.jpa;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.AdminUserDao;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.RoleEntity;

@Named
@ApplicationScoped
public class JpaAdminUserDao extends JpaBaseDao<AdminUserEntity> implements AdminUserDao {

	@Override
	public List<RoleEntity> findRolesForUserById(Long id) {
		return em.createQuery("select e.roles from AdminUserEntity e where e.id = :id", RoleEntity.class)
				.setParameter("id", id).getResultList();
	}

	@Override
	public Class<AdminUserEntity> getEntityClass() {
		return AdminUserEntity.class;
	}

}
