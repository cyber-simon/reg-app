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
package edu.kit.scc.webreg.dao.jpa.as;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.as.ASUserAttrDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;

@Named
@ApplicationScoped
public class JpaASUserAttrDao extends JpaBaseDao<ASUserAttrEntity, Long> implements ASUserAttrDao {

	@Override 
	public ASUserAttrEntity findASUserAttr(UserEntity user, AttributeSourceEntity attributeSource) {
		try {
			return (ASUserAttrEntity) em.createQuery("select a from ASUserAttrEntity a where "
					+ "a.user = :user and a.attributeSource = :attributeSource")
					.setParameter("user", user).setParameter("attributeSource", attributeSource).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override 
	public List<ASUserAttrEntity> findForUser(UserEntity user) {
		return (List<ASUserAttrEntity>) em.createQuery("select a from ASUserAttrEntity a where "
					+ "a.user = :user")
					.setParameter("user", user).getResultList();
	}
	
	@Override
	public Class<ASUserAttrEntity> getEntityClass() {
		return ASUserAttrEntity.class;
	}
}
