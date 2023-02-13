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

import edu.kit.scc.webreg.dao.GroupAdminRoleDao;
import edu.kit.scc.webreg.entity.GroupAdminRoleEntity;

@Named
@ApplicationScoped
public class JpaGroupAdminRoleDao extends JpaBaseDao<GroupAdminRoleEntity> implements GroupAdminRoleDao {

	@Override
	public Class<GroupAdminRoleEntity> getEntityClass() {
		return GroupAdminRoleEntity.class;
	}

}
