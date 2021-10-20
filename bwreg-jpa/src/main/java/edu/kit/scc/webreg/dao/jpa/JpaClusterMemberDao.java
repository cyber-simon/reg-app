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
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ClusterMemberDao;
import edu.kit.scc.webreg.entity.ClusterMemberEntity;
import edu.kit.scc.webreg.entity.ClusterMemberEntity_;
import edu.kit.scc.webreg.entity.ClusterMemberStatus;
import edu.kit.scc.webreg.entity.ClusterSchedulerStatus;

@Named
@ApplicationScoped
public class JpaClusterMemberDao extends JpaBaseDao<ClusterMemberEntity> implements ClusterMemberDao {

	@Override
	public ClusterMemberEntity findByNodeName(String nodename) {
		try {
			return (ClusterMemberEntity) em.createQuery("select e from ClusterMemberEntity e " +
					"where e.nodeName = :nodeName")
					.setParameter("nodeName", nodename)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public List<ClusterMemberEntity> findBySchedulerStatus(ClusterSchedulerStatus status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ClusterMemberEntity> criteria = builder.createQuery(ClusterMemberEntity.class);
		Root<ClusterMemberEntity> root = criteria.from(ClusterMemberEntity.class);
		criteria.where(builder.equal(root.get(ClusterMemberEntity_.clusterSchedulerStatus), status));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}	

	@Override
	public List<ClusterMemberEntity> findByMemberStatus(ClusterMemberStatus status) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ClusterMemberEntity> criteria = builder.createQuery(ClusterMemberEntity.class);
		Root<ClusterMemberEntity> root = criteria.from(ClusterMemberEntity.class);
		criteria.where(builder.equal(root.get(ClusterMemberEntity_.clusterMemberStatus), status));
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}	

	@Override
    public Class<ClusterMemberEntity> getEntityClass() {
		return ClusterMemberEntity.class;
	}
}
