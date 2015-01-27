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
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.BusinessRulePackageDao;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;

@Named
@ApplicationScoped
public class JpaBusinessRulePackageDao extends JpaBaseDao<BusinessRulePackageEntity, Long> implements BusinessRulePackageDao {

    @Override
    @SuppressWarnings({"unchecked"})
	public List<BusinessRulePackageEntity> findAllNewer(Date date) {
		return em.createQuery("select e from BusinessRulePackageEntity e where dirtyStamp > :date")
				.setParameter("date", date).getResultList();
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public List<BusinessRulePackageEntity> findAllWithRules() {
		return em.createQuery("select distinct e from BusinessRulePackageEntity e left join fetch e.rules")
				.getResultList();
	}

    @Override
	public BusinessRulePackageEntity findByNameAndVersion(String baseName, String version) {
    	try {
    		return (BusinessRulePackageEntity) em.createQuery("select e from BusinessRulePackageEntity e where knowledgeBaseName = :kbn and "
				+ "knowledgeBaseVersion = :kbv")
				.setParameter("kbn", baseName)
				.setParameter("kbv", version)
				.getSingleResult();
    	}
    	catch (NoResultException nre) {
    		return null;
    	}
	}

	@Override
	public Class<BusinessRulePackageEntity> getEntityClass() {
		return BusinessRulePackageEntity.class;
	}
}
