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

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.Query;

import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;

@Named
@ApplicationScoped
public class JpaUserLoginInfoDao extends JpaBaseDao<UserLoginInfoEntity> implements UserLoginInfoDao {

	@Override
	public void deleteLoginInfo(long millis) {
		Query query = em.createQuery("delete from UserLoginInfoEntity where loginDate <= :loginDate");
		query.setParameter("loginDate", new Date(System.currentTimeMillis() - millis));
		query.executeUpdate();
	}

	@Override
	public Class<UserLoginInfoEntity> getEntityClass() {
		return UserLoginInfoEntity.class;
	}

}
