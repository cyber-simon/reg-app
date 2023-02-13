/*******************************************************************************
 * Copyright (c) 2021 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.dao.jpa.oidc;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcGroupDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcGroupEntity;

@Named
@ApplicationScoped
public class JpaOidcGroupDao extends JpaBaseDao<OidcGroupEntity> implements OidcGroupDao {

	@Override
	public List<OidcGroupEntity> findByUser(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OidcGroupEntity> criteria = builder.createQuery(OidcGroupEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
		criteria.where(builder.equal(userRoot.get("id"), user.getId()));
		Join<UserEntity, OidcGroupEntity> users = userRoot.join("groups");
		CriteriaQuery<OidcGroupEntity> cq = criteria.select(users);
		TypedQuery<OidcGroupEntity> query = em.createQuery(cq);
		return query.getResultList();
	}

	@Override
	public Class<OidcGroupEntity> getEntityClass() {
		return OidcGroupEntity.class;
	}

}
