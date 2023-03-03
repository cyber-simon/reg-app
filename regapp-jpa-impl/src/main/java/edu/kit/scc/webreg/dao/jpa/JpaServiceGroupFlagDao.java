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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.entity.GroupStatus;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity_;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;

@Named
@ApplicationScoped
public class JpaServiceGroupFlagDao extends JpaBaseDao<ServiceGroupFlagEntity> implements ServiceGroupFlagDao {

	@Override
	public List<ServiceGroupFlagEntity> findByGroup(ServiceBasedGroupEntity group) {
		return findAll(equal(ServiceGroupFlagEntity_.group, group));
	}

	@Override
	public List<ServiceGroupFlagEntity> findByService(ServiceEntity service) {
		return findAll(equal(ServiceGroupFlagEntity_.service, service));
	}

	@Override
	public List<ServiceGroupFlagEntity> findLocalGroupsForService(ServiceEntity service) {
		return em.createQuery(
				"select gf from ServiceGroupFlagEntity gf, LocalGroupEntity g where gf.group = g and gf.service = :service and (g.groupStatus = :groupStatus or g.groupStatus is null)",
				ServiceGroupFlagEntity.class).setParameter("service", service)
				.setParameter("groupStatus", GroupStatus.ACTIVE).getResultList();
	}

	@Override
	public List<ServiceGroupFlagEntity> findByStatus(ServiceGroupStatus status) {
		return findAll(equal(ServiceGroupFlagEntity_.status, status));
	}

	@Override
	public List<ServiceGroupFlagEntity> findByGroupAndService(ServiceBasedGroupEntity group, ServiceEntity service) {
		return findAll(
				and(equal(ServiceGroupFlagEntity_.group, group), equal(ServiceGroupFlagEntity_.service, service)));
	}

	@Override
	public Class<ServiceGroupFlagEntity> getEntityClass() {
		return ServiceGroupFlagEntity.class;
	}

}
