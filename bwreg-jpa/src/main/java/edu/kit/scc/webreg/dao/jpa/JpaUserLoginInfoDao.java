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
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.Query;

import edu.kit.scc.webreg.dao.UserLoginInfoDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginMethod;

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
	@SuppressWarnings("unchecked")
	public List<UserLoginInfoEntity> findByUserList(List<UserEntity> userList) {
		return em.createQuery("select e from UserLoginInfoEntity e where e.user in :userList")
				.setParameter("userList", userList).getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<UserLoginInfoEntity> findByIdentity(Long identityId) {
		return em.createQuery("select e from UserLoginInfoEntity e where e.identity.id = :identityId")
				.setParameter("identityId", identityId).getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<UserLoginInfoEntity> findByRegistry(Long registryId) {
		return em.createQuery("select e from UserLoginInfoEntity e where e.registry.id = :registryId")
				.setParameter("registryId", registryId).getResultList();	
	}

	@Override
	@SuppressWarnings("unchecked")
	public UserLoginInfoEntity findLastByRegistryAndMethod(Long registryId, UserLoginMethod method) {
		List<UserLoginInfoEntity> list = em.createQuery(
				"select e from UserLoginInfoEntity e where e.registry.id = :registryId "
				+ "and e.loginMethod = :loginMethod order by e.loginDate desc")
				.setParameter("registryId", registryId)
				.setParameter("loginMethod", method)
				.setMaxResults(1)
				.getResultList();
		if (list.size() == 0) {
			return null;
		}
		else {
			return list.get(0);
		}
	}
	
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
