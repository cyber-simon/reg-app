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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.AdminRoleDao;
import edu.kit.scc.webreg.entity.AdminRoleEntity;

@Named
@ApplicationScoped
public class JpaAdminRoleDao extends JpaBaseDao<AdminRoleEntity> implements AdminRoleDao {

	@Override
	public Class<AdminRoleEntity> getEntityClass() {
		return AdminRoleEntity.class;
	}

}
