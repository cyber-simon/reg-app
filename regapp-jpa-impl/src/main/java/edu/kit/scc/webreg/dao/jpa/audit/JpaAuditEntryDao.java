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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.audit.AuditEntryEntity;
import edu.kit.scc.webreg.entity.audit.AuditServiceRegisterEntity;

@Named
@ApplicationScoped
public class JpaAuditEntryDao extends JpaBaseDao<AuditEntryEntity> implements AuditEntryDao {

	@Override
	public List<AuditServiceRegisterEntity> findAllServiceRegister(RegistryEntity registry) {
		return em.createQuery("select e from AuditServiceRegisterEntity e where e.registry = :registry",
				AuditServiceRegisterEntity.class).setParameter("registry", registry).getResultList();
	}

	@Override
	public Class<AuditEntryEntity> getEntityClass() {
		return AuditEntryEntity.class;
	}

}
