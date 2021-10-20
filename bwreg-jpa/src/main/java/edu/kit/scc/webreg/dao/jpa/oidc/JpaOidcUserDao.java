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
package edu.kit.scc.webreg.dao.jpa.oidc;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcUserDao;
import edu.kit.scc.webreg.entity.oidc.OidcRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity_;

@Named
@ApplicationScoped
public class JpaOidcUserDao extends JpaBaseDao<OidcUserEntity> implements OidcUserDao, Serializable {

	private static final long serialVersionUID = 1L;
    
	@Override
	public OidcUserEntity findByIssuerAndSub(OidcRpConfigurationEntity issuer, String subjectId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcUserEntity> criteria = builder.createQuery(OidcUserEntity.class);
		Root<OidcUserEntity> user = criteria.from(OidcUserEntity.class);
		criteria.where(builder.and(
				builder.equal(user.get(OidcUserEntity_.issuer), issuer),
				builder.equal(user.get(OidcUserEntity_.subjectId), subjectId)
				));
		criteria.select(user);
		criteria.distinct(true);
		
		try {
			return em.createQuery(criteria).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}			
	}
		
	@Override
	public Class<OidcUserEntity> getEntityClass() {
		return OidcUserEntity.class;
	}
}
