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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.in;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.notIn;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryEntity_;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@Named
@ApplicationScoped
public class JpaRegistryDao extends JpaBaseDao<RegistryEntity> implements RegistryDao {

	@Override
	public List<RegistryEntity> findAllByRegValueAndStatus(ServiceEntity service, String key, String value,
			RegistryStatus status) {
		return em
				.createQuery("select r from RegistryEntity r join r.registryValues rv "
						+ "where (key(rv) = :key and rv = :val) "
						+ "and r.service = :service and r.registryStatus = :status", RegistryEntity.class)
				.setParameter("key", key).setParameter("val", value).setParameter("service", service)
				.setParameter("status", status).getResultList();
	}

	@Override
	public List<RegistryEntity> findByService(ServiceEntity service) {
		return findAll(equal(RegistryEntity_.service, service));
	}

	@Override
	public List<RegistryEntity> findAllExternalBySsn(String serviceShortName) {
		return em
				.createQuery("select r from RegistryEntity r, UserEntity u where r.service.shortName = :ssn and "
						+ "r.user = u and TYPE(u) = :class", RegistryEntity.class)
				.setParameter("ssn", serviceShortName).setParameter("class", ExternalUserEntity.class).getResultList();
	}

	@Override
	public List<RegistryEntity> findRegistriesForDepro(String serviceShortName) {
		return em.createQuery(
				"select r from RegistryEntity r where r.service.shortName = :ssn and r.registryStatus = :status and "
						+ "r.agreedTime = (select max(r1.agreedTime) from RegistryEntity r1 where r1.user = r.user and r1.service = r.service) and not exists "
						+ "(select r2 from RegistryEntity r2 where r2.user = r.user and r2.agreedTime > r.agreedTime and r2.service = r.service)",
				RegistryEntity.class).setParameter("ssn", serviceShortName)
				.setParameter("status", RegistryStatus.DELETED).getResultList();
	}

	@Override
	public RegistryEntity findRegistryForDepro(String serviceShortName, String key, String value) {
		try {
			return em.createQuery("select r from RegistryEntity r join r.registryValues rv "
					+ "where (key(rv) = :key and rv = :val) and r.service.shortName = :ssn and r.registryStatus = :status and "
					+ "r.agreedTime = (select max(r1.agreedTime) from RegistryEntity r1 where r1.user = r.user and r1.service = r.service) and not exists "
					+ "(select r2 from RegistryEntity r2 where r2.user = r.user and r2.agreedTime > r.agreedTime and r2.service = r.service)",
					RegistryEntity.class).setParameter("key", key).setParameter("val", value)
					.setParameter("ssn", serviceShortName).setParameter("status", RegistryStatus.DELETED)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<UserEntity> findUserListByServiceAndStatus(ServiceEntity service, RegistryStatus status) {
		return em
				.createQuery(
						"select r.user from RegistryEntity r where r.service = :service and r.registryStatus = :status",
						UserEntity.class)
				.setParameter("service", service).setParameter("status", status).getResultList();
	}

	@Override
	public List<RegistryEntity> findByServiceAndStatus(ServiceEntity service, RegistryStatus status) {
		return findAll(
				and(equal(RegistryEntity_.service, service), equal(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public List<RegistryEntity> findByServiceAndAttribute(String key, String value, ServiceEntity service) {
		return em
				.createQuery(
						"select r from RegistryEntity r join r.registryValues rv "
								+ "where (key(rv) = :key and rv = :val) " + "and r.service = :service ",
						RegistryEntity.class)
				.setParameter("key", key).setParameter("val", value).setParameter("service", service).getResultList();
	}

	@Override
	public List<RegistryEntity> findByServiceAndNotStatus(ServiceEntity service, RegistryStatus... status) {
		return findAll(
				and(equal(RegistryEntity_.service, service), notIn(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public RegistryEntity findByServiceAndUserAndStatus(ServiceEntity service, UserEntity user, RegistryStatus status) {
		return find(and(equal(RegistryEntity_.service, service), equal(RegistryEntity_.user, user),
				equal(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public RegistryEntity findByServiceAndIdentityAndStatus(ServiceEntity service, IdentityEntity identity,
			RegistryStatus status) {
		return find(and(equal(RegistryEntity_.service, service), equal(RegistryEntity_.identity, identity),
				equal(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public List<RegistryEntity> findByServiceAndIdentityAndNotStatus(ServiceEntity service, IdentityEntity identity,
			RegistryStatus... status) {
		return findAll(and(equal(RegistryEntity_.service, service), equal(RegistryEntity_.identity, identity),
				notIn(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public List<RegistryEntity> findByIdentityAndStatus(IdentityEntity identity, RegistryStatus... status) {
		return findAll(
				and(equal(RegistryEntity_.identity, identity), in(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public List<RegistryEntity> findByUserAndStatus(UserEntity user, RegistryStatus... status) {
		return findAll(and(equal(RegistryEntity_.user, user), in(RegistryEntity_.registryStatus, status)));
	}

	@Override
	public List<RegistryEntity> findByIdentityAndNotStatusAndNotHidden(IdentityEntity identity,
			RegistryStatus... status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<RegistryEntity> criteria = builder.createQuery(RegistryEntity.class);
		Root<RegistryEntity> root = criteria.from(RegistryEntity.class);
		Join<RegistryEntity, ServiceEntity> serviceJoin = root.join(RegistryEntity_.service);

		List<Predicate> predList = new ArrayList<Predicate>();

		predList.add(builder.or(builder.isNull(serviceJoin.get(ServiceEntity_.hidden)),
				builder.equal(serviceJoin.get(ServiceEntity_.hidden), false)));

		predList.add(builder.equal(root.get(RegistryEntity_.identity), identity));
		for (RegistryStatus s : status)
			predList.add(builder.notEqual(root.get(RegistryEntity_.registryStatus), s));

		criteria.where(builder.and(predList.toArray(new Predicate[] {})));
		criteria.select(root);
		criteria.distinct(true);

		return em.createQuery(criteria).getResultList();
	}

	@Override
	public List<RegistryEntity> findByUser(UserEntity user) {
		return findAll(equal(RegistryEntity_.user, user));
	}

	@Override
	public Class<RegistryEntity> getEntityClass() {
		return RegistryEntity.class;
	}

}
