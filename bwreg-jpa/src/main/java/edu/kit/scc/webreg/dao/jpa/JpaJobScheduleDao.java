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

import edu.kit.scc.webreg.dao.JobScheduleDao;
import edu.kit.scc.webreg.entity.JobScheduleEntity;

@Named
@ApplicationScoped
public class JpaJobScheduleDao extends JpaBaseDao<JobScheduleEntity, Long> implements JobScheduleDao {

    @Override
    @SuppressWarnings({"unchecked"})
	public List<JobScheduleEntity> findAllBySingleton(Boolean singleton, Boolean disabled) {
		return em.createQuery("select e from JobScheduleEntity e where e.jobClass.singleton = :singleton " +
				"and e.disabled = :disabled")
				.setParameter("disabled", disabled)
				.setParameter("singleton", singleton).getResultList();
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<JobScheduleEntity> findAllBySingletonNewer(Boolean singleton, Date date) {
		return em.createQuery("select e from JobScheduleEntity e where e.jobClass.singleton = :singleton " +
				"and e.updatedAt > :date")
				.setParameter("date", date)
				.setParameter("singleton", singleton).getResultList();
	}

	@Override
	public Class<JobScheduleEntity> getEntityClass() {
		return JobScheduleEntity.class;
	}
}
