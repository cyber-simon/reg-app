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

import edu.kit.scc.webreg.dao.as.AttributeSourceGroupDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceGroupEntity;

@Named
@ApplicationScoped
public class JpaAttributeSourceGroupDao extends JpaBaseDao<AttributeSourceGroupEntity, Long> implements AttributeSourceGroupDao {

	@SuppressWarnings("unchecked")
	@Override
	public List<AttributeSourceGroupEntity> findByUserAndAS(UserEntity user, AttributeSourceEntity attributeSource) {
		return (List<AttributeSourceGroupEntity>) em.createQuery("select e from AttributeSourceGroupEntity e left join e.users as ug"
				+ " where ug.user = :user"
				+ " and e.attributeSource = :attributeSource")
				.setParameter("user", user)
				.setParameter("attributeSource", attributeSource)
				.getResultList();
	}
	
	@Override
	public AttributeSourceGroupEntity findByNameAndAS(String name, AttributeSourceEntity attributeSource) {
		try {
			return (AttributeSourceGroupEntity) em.createQuery("select e from AttributeSourceGroupEntity e where e.name = :name"
					+ " and e.attributeSource = :attributeSource")
				.setParameter("name", name)
				.setParameter("attributeSource", attributeSource)
				.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Class<AttributeSourceGroupEntity> getEntityClass() {
		return AttributeSourceGroupEntity.class;
	}
}
