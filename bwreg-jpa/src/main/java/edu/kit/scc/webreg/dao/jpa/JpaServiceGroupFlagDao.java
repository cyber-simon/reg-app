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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.entity.GroupStatus;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;

@Named
@ApplicationScoped
public class JpaServiceGroupFlagDao extends JpaBaseDao<ServiceGroupFlagEntity, Long> implements ServiceGroupFlagDao {

    @Override
	public List<ServiceGroupFlagEntity> findByGroup(ServiceBasedGroupEntity group) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceGroupFlagEntity> criteria = builder.createQuery(ServiceGroupFlagEntity.class);
		Root<ServiceGroupFlagEntity> root = criteria.from(ServiceGroupFlagEntity.class);
		criteria.where(
				builder.equal(root.get("group"), group));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
    public List<ServiceGroupFlagEntity> findByGroupAndStatus(ServiceBasedGroupEntity group, ServiceGroupStatus status) {
		return em.createQuery("select gf from ServiceGroupFlagEntity gf "
				+ "where gf.status = :status and gf.group = :group")
				.setParameter("status", status).setParameter("group", group).getResultList();
    }
    
    @Override
	public List<ServiceGroupFlagEntity> findByService(ServiceEntity service) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceGroupFlagEntity> criteria = builder.createQuery(ServiceGroupFlagEntity.class);
		Root<ServiceGroupFlagEntity> root = criteria.from(ServiceGroupFlagEntity.class);
		criteria.where(
				builder.equal(root.get("service"), service));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
    public List<ServiceGroupFlagEntity> findLocalGroupsForService(ServiceEntity service) {
		return em.createQuery("select gf from ServiceGroupFlagEntity gf, LocalGroupEntity g "
				+ "where gf.group = g"
				+ " and gf.service = :service and (g.groupStatus = :groupStatus or g.groupStatus is null)")
				.setParameter("service", service)
				.setParameter("groupStatus", GroupStatus.ACTIVE)
				.getResultList();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<ServiceGroupFlagEntity> findByStatus(ServiceGroupStatus status) {
		return em.createQuery("select gf from ServiceGroupFlagEntity gf "
				+ "where gf.status = :status")
				.setParameter("status", status).getResultList();
    }

    @Override
	public List<ServiceGroupFlagEntity> findByGroupAndService(ServiceBasedGroupEntity group, ServiceEntity service) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ServiceGroupFlagEntity> criteria = builder.createQuery(ServiceGroupFlagEntity.class);
		Root<ServiceGroupFlagEntity> root = criteria.from(ServiceGroupFlagEntity.class);
		criteria.where(builder.and(
				builder.equal(root.get("group"), group),
				builder.equal(root.get("service"), service)
				));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

	@Override
	public Class<ServiceGroupFlagEntity> getEntityClass() {
		return ServiceGroupFlagEntity.class;
	}
}
