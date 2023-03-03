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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.TypedQuery;

import edu.kit.scc.webreg.dao.LocalGroupDao;
import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.entity.GroupStatus;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.LocalGroupEntity_;
import edu.kit.scc.webreg.entity.ServiceEntity;

@Named
@ApplicationScoped
public class JpaLocalGroupDao extends JpaBaseDao<LocalGroupEntity> implements LocalGroupDao {

	@Override
	public LocalGroupEntity findByName(String name) {
		return find(equal(LocalGroupEntity_.name, name));
	}

	@Override
	public List<LocalGroupEntity> findAllActiveGroupsByService(PaginateBy paginateBy, ServiceEntity service) {
		TypedQuery<LocalGroupEntity> query = em
				.createQuery("select g from ServiceGroupFlagEntity gf, LocalGroupEntity g "
						+ "where gf.group = g and gf.service = :service and (g.groupStatus = :groupStatus or g.groupStatus is null) "
						+ "order by g.id asc", LocalGroupEntity.class)
				.setParameter("service", service).setParameter("groupStatus", GroupStatus.ACTIVE);
		if (paginateBy != null) {
			applyPaging(query, paginateBy);
		}
		return query.getResultList();
	}

	@Override
	public Number countAllByService(ServiceEntity service) {
		return em.createQuery("select count(g) from ServiceGroupFlagEntity gf, LocalGroupEntity g "
				+ "where gf.group = g and gf.service = :service and (g.groupStatus = :groupStatus or g.groupStatus is null)",
				Number.class).setParameter("service", service).setParameter("groupStatus", GroupStatus.ACTIVE)
				.getSingleResult();
	}

	@Override
	public Class<LocalGroupEntity> getEntityClass() {
		return LocalGroupEntity.class;
	}

}
