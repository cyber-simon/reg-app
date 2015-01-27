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

import edu.kit.scc.webreg.dao.ServiceEventDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEventEntity;

@Named
@ApplicationScoped
public class JpaServiceEventDao extends JpaBaseDao<ServiceEventEntity, Long> implements ServiceEventDao {

    @Override
	public Class<ServiceEventEntity> getEntityClass() {
		return ServiceEventEntity.class;
	}
    
	@Override
    @SuppressWarnings({"unchecked"})
	public List<ServiceEventEntity> findAllByService(ServiceEntity service) {
		return em.createQuery("select e from ServiceEventEntity e where e.service = :service")
				.setParameter("service", service).getResultList();
	}

}
