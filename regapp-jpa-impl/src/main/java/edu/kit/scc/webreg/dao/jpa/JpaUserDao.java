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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Root;

@Named
@ApplicationScoped
public class JpaUserDao extends JpaBaseDao<UserEntity> implements UserDao {

	@Override
	public List<UserEntity> findByIdentity(IdentityEntity identity) {
		return findAll(equal(UserEntity_.identity, identity));
	}

	@Override
	public List<UserEntity> findByAttribute(String key, String value) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> root = criteria.from(UserEntity.class);
		criteria.select(root);
		MapJoin<UserEntity, String, String> mapJoin = root.joinMap("attributeStore");
		criteria.where(builder.and(builder.equal(mapJoin.key(), key), builder.equal(mapJoin.value(), value)));

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public List<UserEntity> findByGeneric(String key, String value) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<UserEntity> criteria = builder.createQuery(UserEntity.class);
		Root<UserEntity> root = criteria.from(UserEntity.class);
		criteria.select(root);
		MapJoin<UserEntity, String, String> mapJoin = root.joinMap("genericStore");
		criteria.where(builder.and(builder.equal(mapJoin.key(), key), builder.equal(mapJoin.value(), value)));

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public List<UserEntity> findByGroup(GroupEntity group) {
		return em.createQuery("select e.user from UserGroupEntity e where e.group = :group", UserEntity.class)
				.setParameter("group", group).getResultList();
	}

	@Override
	public List<UserEntity> findByGroupAndService(GroupEntity group, ServiceEntity service,
			RegistryStatus registryStatus) {
		return em.createQuery("select e.user from UserGroupEntity e, RegistryEntity re "
				+ "where e.group = :group and re.user = e.user and re.registryStatus = :registryStatus and re.service = :service",
				UserEntity.class).setParameter("group", group).setParameter("service", service)
				.setParameter("registryStatus", registryStatus).getResultList();
	}

	@Override
	public List<UserEntity> findByEppn(String eppn) {
		return findAll(equal(UserEntity_.eppn, eppn));
	}

	@Override
	public UserEntity findByUidNumber(Integer uidNumber) {
		return find(equal(UserEntity_.uidNumber, uidNumber));
	}

	@Override
	public UserEntity findByIdWithAll(Long id) {
		return find(equal(UserEntity_.id, id), UserEntity_.roles, UserEntity_.groups, UserEntity_.genericStore,
				UserEntity_.attributeStore);
	}

	@Override
	public UserEntity findByIdWithStore(Long id) {
		return find(equal(UserEntity_.id, id), UserEntity_.genericStore, UserEntity_.attributeStore);
	}

	@Override
	public Class<UserEntity> getEntityClass() {
		return UserEntity.class;
	}

}
