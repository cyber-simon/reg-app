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

import edu.kit.scc.webreg.dao.EventDao;
import edu.kit.scc.webreg.entity.EventEntity;
import edu.kit.scc.webreg.entity.EventType;

@Named
@ApplicationScoped
public class JpaEventDao extends JpaBaseDao<EventEntity, Long> implements EventDao {

    @Override
	public Class<EventEntity> getEntityClass() {
		return EventEntity.class;
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<EventEntity> findAllByEventType(EventType eventType) {
		return em.createQuery("select e from EventEntity e where e.eventType = :eventType")
				.setParameter("eventType", eventType).getResultList();
	}
    
}
