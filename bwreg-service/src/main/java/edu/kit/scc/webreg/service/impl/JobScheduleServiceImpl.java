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
package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.JobScheduleDao;
import edu.kit.scc.webreg.entity.JobScheduleEntity;
import edu.kit.scc.webreg.entity.JobScheduleEntity_;
import edu.kit.scc.webreg.service.JobScheduleService;

@Stateless
public class JobScheduleServiceImpl extends BaseServiceImpl<JobScheduleEntity> implements JobScheduleService {

	private static final long serialVersionUID = 1L;

	@Inject
	private JobScheduleDao dao;

	@Override
	public List<JobScheduleEntity> findAllBySingleton(Boolean singleton, Boolean disabled) {
		return dao.findAll(
				and(equal("jobClass.singleton", singleton), equal(JobScheduleEntity_.disabled, disabled)));
	}

	@Override
	public List<JobScheduleEntity> findAllBySingletonNewer(Boolean singleton, Date date) {
		return dao
				.findAll(and(equal("jobClass.singleton", singleton), equal(JobScheduleEntity_.updatedAt, date)));

	}

	@Override
	protected BaseDao<JobScheduleEntity> getDao() {
		return dao;
	}

}
