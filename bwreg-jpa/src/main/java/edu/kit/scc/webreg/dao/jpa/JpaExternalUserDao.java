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

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.ExternalUserDao;
import edu.kit.scc.webreg.entity.ExternalUserAdminRoleEntity;
import edu.kit.scc.webreg.entity.ExternalUserEntity;

@Named
@ApplicationScoped
public class JpaExternalUserDao extends JpaBaseDao<ExternalUserEntity, Long> implements ExternalUserDao, Serializable {

	private static final long serialVersionUID = 1L;
    
	@Override
	public ExternalUserEntity findByExternalId(String externalId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<ExternalUserEntity> criteria = builder.createQuery(ExternalUserEntity.class);
		Root<ExternalUserEntity> user = criteria.from(ExternalUserEntity.class);
		criteria.where(builder.equal(user.get("externalId"), externalId));
		criteria.select(user);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ExternalUserEntity> findByAdmin(ExternalUserAdminRoleEntity adminRole) {
		return em.createQuery("select e from ExternalUserEntity e where e.admin = :admin")
				.setParameter("admin", adminRole).getResultList();
	}
		
	@Override
	public Class<ExternalUserEntity> getEntityClass() {
		return ExternalUserEntity.class;
	}
}
