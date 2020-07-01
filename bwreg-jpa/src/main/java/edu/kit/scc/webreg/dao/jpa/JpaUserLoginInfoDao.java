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

import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;

@Named
@ApplicationScoped
public class JpaUserLoginInfoDao extends JpaBaseDao<UserLoginInfoEntity, Long> implements UserLoginInfoDao {

	@Override
	@SuppressWarnings("unchecked")
	public List<UserLoginInfoEntity> findByUser(Long userId) {
		return em.createQuery("select e from UserLoginInfoEntity e where e.user.id = :userId")
				.setParameter("userId", userId).getResultList();	
	}

	@Override
    public Class<UserLoginInfoEntity> getEntityClass() {
		return UserLoginInfoEntity.class;
	}
}
