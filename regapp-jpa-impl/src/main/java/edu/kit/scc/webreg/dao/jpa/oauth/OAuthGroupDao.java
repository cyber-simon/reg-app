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
package edu.kit.scc.webreg.dao.jpa.oauth;

import java.util.List;

import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthGroupEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;

@Named
@ApplicationScoped
public class OAuthGroupDao extends JpaBaseDao<OAuthGroupEntity> {

	public List<OAuthGroupEntity> findByUser(UserEntity user) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<OAuthGroupEntity> criteria = builder.createQuery(OAuthGroupEntity.class);
		Root<UserEntity> userRoot = criteria.from(UserEntity.class);
		criteria.where(builder.equal(userRoot.get("id"), user.getId()));
		Join<UserEntity, OAuthGroupEntity> users = userRoot.join("groups");
		CriteriaQuery<OAuthGroupEntity> cq = criteria.select(users);
		TypedQuery<OAuthGroupEntity> query = em.createQuery(cq);
		return query.getResultList();
	}

	@Override
	public Class<OAuthGroupEntity> getEntityClass() {
		return OAuthGroupEntity.class;
	}

}
