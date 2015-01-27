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

import edu.kit.scc.webreg.dao.BusinessRuleDao;
import edu.kit.scc.webreg.entity.BusinessRuleEntity;

@Named
@ApplicationScoped
public class JpaBusinessRuleDao extends JpaBaseDao<BusinessRuleEntity, Long> implements BusinessRuleDao {

    @Override
    @SuppressWarnings({"unchecked"})
	public List<BusinessRuleEntity> findAllNewer(Date date) {
		return em.createQuery("select e from BusinessRuleEntity e where updatedAt > :date")
				.setParameter("date", date).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public List<BusinessRuleEntity> findAllKnowledgeBaseNotNull() {
		return em.createQuery("select e from BusinessRuleEntity e where knowledgeBaseName != null")
				.getResultList();
	}

	@Override
	public Class<BusinessRuleEntity> getEntityClass() {
		return BusinessRuleEntity.class;
	}
}
