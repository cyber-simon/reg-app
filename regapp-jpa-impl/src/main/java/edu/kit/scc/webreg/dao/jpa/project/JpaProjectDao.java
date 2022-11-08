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
package edu.kit.scc.webreg.dao.jpa.project;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;

@Named
@ApplicationScoped
public class JpaProjectDao extends JpaBaseProjectDao<ProjectEntity> implements ProjectDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<ProjectEntity> findByService(ServiceEntity service) {
		return em.createQuery("select r.project from ProjectEntity r where r.service = :service order by r.project.name")
			.setParameter("service", service).getResultList();
	}

	@Override
	public Class<ProjectEntity> getEntityClass() {
		return ProjectEntity.class;
	}
}
