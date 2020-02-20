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
package edu.kit.scc.webreg.service.project;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class ProjectServiceImpl extends BaseServiceImpl<ProjectEntity, Long> implements ProjectService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ProjectDao dao;
	
	@Override
	public List<ProjectEntity> findByService(ServiceEntity service) {
		return dao.findByService(service);
	}
	
	@Override
	protected BaseDao<ProjectEntity, Long> getDao() {
		return dao;
	}
}
