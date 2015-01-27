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

import edu.kit.scc.webreg.dao.UserEventDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.UserEventEntity;

@Named
@ApplicationScoped
public class JpaUserEventDao extends JpaBaseDao<UserEventEntity, Long> implements UserEventDao {

    @Override
	public Class<UserEventEntity> getEntityClass() {
		return UserEventEntity.class;
	}

	@Override
    @SuppressWarnings({"unchecked"})
	public List<UserEventEntity> findAllByEventType(EventType eventType) {
		return em.createQuery("select e from UserEventEntity e where e.eventType = :eventType")
				.setParameter("eventType", eventType).getResultList();
	}
    
}
