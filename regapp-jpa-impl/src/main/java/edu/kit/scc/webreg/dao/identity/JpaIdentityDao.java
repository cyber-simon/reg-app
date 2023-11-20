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
package edu.kit.scc.webreg.dao.identity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@Named
@ApplicationScoped
public class JpaIdentityDao extends JpaBaseDao<IdentityEntity> implements IdentityDao {

	@Inject
	private UserDao userDao;

	@Override
	public IdentityEntity findByUserId(Long userId) {
		UserEntity user = userDao.fetch(userId);
		return user == null ? null : user.getIdentity();
	}

	@Override
	public Class<IdentityEntity> getEntityClass() {
		return IdentityEntity.class;
	}

}
