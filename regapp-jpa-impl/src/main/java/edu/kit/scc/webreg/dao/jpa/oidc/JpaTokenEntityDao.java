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
import javax.persistence.Query;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcTokenDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcTokenEntity;

@Named
@ApplicationScoped
public class JpaTokenEntityDao extends JpaBaseDao<OidcTokenEntity> implements OidcTokenDao, Serializable {

	private static final long serialVersionUID = 1L;
    
	@Override
	public void deleteTokenForUser(UserEntity user) {
		Query query = em.createQuery("delete from OidcTokenEntity where user=:user");
		query.setParameter("user", user);
		query.executeUpdate();
	}

	@Override
	public OidcTokenEntity findByUserId(Long userId) {
		try {
			return (OidcTokenEntity) em.createQuery("select e from OidcTokenEntity e " +
					"where e.user.id = :userId")
					.setParameter("userId", userId)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public Class<OidcTokenEntity> getEntityClass() {
		return OidcTokenEntity.class;
	}
}
