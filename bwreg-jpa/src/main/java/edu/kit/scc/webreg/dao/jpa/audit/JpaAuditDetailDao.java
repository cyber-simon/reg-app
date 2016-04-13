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
package edu.kit.scc.webreg.dao.jpa.audit;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.audit.AuditDetailEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;

@Named
@ApplicationScoped
public class JpaAuditDetailDao extends JpaBaseDao<AuditDetailEntity, Long> implements AuditDetailDao {

    @Override
    @SuppressWarnings({"unchecked"})
	public List<AuditDetailEntity> findNewestFailed(int limit) {
		return em.createQuery("select e from AuditDetailEntity e where e.auditStatus = :status" +
				" order by e.endTime desc")
				.setParameter("status", AuditStatus.FAIL)
				.setMaxResults(limit).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public List<AuditDetailEntity> findAllByAuditEntry(AuditEntryEntity auditEntry) {
		return em.createQuery("select e from AuditDetailEntity e where e.auditEntry = :auditEntry" +
				" order by e.endTime asc")
				.setParameter("auditEntry", auditEntry)
				.getResultList();
	}

	@Override
	public Class<AuditDetailEntity> getEntityClass() {
		return AuditDetailEntity.class;
	}
}
