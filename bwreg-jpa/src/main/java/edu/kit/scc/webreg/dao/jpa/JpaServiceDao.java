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
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Named
@ApplicationScoped
public class JpaServiceDao extends JpaBaseDao<ServiceEntity, Long> implements ServiceDao {

    @Override
	public ServiceEntity findByShortName(String shortName) {
		try {
			return (ServiceEntity) em.createQuery("select e from ServiceEntity e where e.shortName = :shortName")
				.setParameter("shortName", shortName).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<ServiceEntity> findAllPublishedWithServiceProps() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.where(
				builder.equal(root.get("published"), true));
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("serviceProps", JoinType.LEFT);

		return em.createQuery(criteria).getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByParentService(ServiceEntity service) {
		return em.createQuery("select e from ServiceEntity e where e.parentService = :service")
				.setParameter("service", service).getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByAdminRole(RoleEntity role) {
		return em.createQuery("select e from ServiceEntity e where e.adminRole = :role")
				.setParameter("role", role).getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByHotlineRole(RoleEntity role) {
		return em.createQuery("select e from ServiceEntity e where e.hotlineRole = :role")
				.setParameter("role", role).getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByApproverRole(RoleEntity role) {
		return em.createQuery("select e from ServiceEntity e where e.approverRole = :role")
				.setParameter("role", role).getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByGroupAdminRole(RoleEntity role) {
		return em.createQuery("select e from ServiceEntity e where e.groupAdminRole = :role")
				.setParameter("role", role).getResultList();
	}
	
	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEntity> findByGroupCapability(Boolean capable) {
		return em.createQuery("select e from ServiceEntity e where e.groupCapable = :capable")
				.setParameter("capable", capable).getResultList();
	}
	
	@Override
	public List<ServiceEntity> findAllWithPolicies() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("policies", JoinType.LEFT);

		return em.createQuery(criteria).getResultList();
	}


	@Override
	public ServiceEntity findWithPolicies(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("policies", JoinType.LEFT);

		return em.createQuery(criteria).getSingleResult();
	}

	@Override
	public List<ServiceEntity> findAllByImage(ImageEntity image) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.select(root);
		criteria.where(builder.equal(root.get("image"), image));

		return em.createQuery(criteria).getResultList();
	}
	
	@Override
	public ServiceEntity findByIdWithServiceProps(Long id) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceEntity> criteria = builder.createQuery(ServiceEntity.class);
		Root<ServiceEntity> root = criteria.from(ServiceEntity.class);
		criteria.where(
				builder.equal(root.get("id"), id));
		criteria.select(root);
		criteria.distinct(true);
		root.fetch("serviceProps", JoinType.LEFT);
		root.fetch("policies", JoinType.LEFT);

		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}			
	}

	@Override
	public Class<ServiceEntity> getEntityClass() {
		return ServiceEntity.class;
	}
}
