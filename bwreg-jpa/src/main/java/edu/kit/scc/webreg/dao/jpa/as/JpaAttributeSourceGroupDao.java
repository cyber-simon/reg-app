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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import edu.kit.scc.webreg.dao.as.AttributeSourceGroupDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceGroupEntity;

@Named
@ApplicationScoped
public class JpaAttributeSourceGroupDao extends JpaBaseDao<AttributeSourceGroupEntity, Long> implements AttributeSourceGroupDao {

	@Override
	public List<AttributeSourceGroupEntity> findByUserAndAS(UserEntity user, AttributeSourceEntity attributeSource) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<AttributeSourceGroupEntity> criteria = builder.createQuery(AttributeSourceGroupEntity.class);
	    Root<UserEntity> userRoot = criteria.from(UserEntity.class);
	    Root<AttributeSourceGroupEntity> asgRoot = criteria.from(AttributeSourceGroupEntity.class);
	    criteria.where(
	    		builder.and(
	    				builder.equal(userRoot.get("id"), user.getId()),
	    				builder.equal(asgRoot.get("id"), attributeSource.getId())
	    		));
	    Join<UserEntity, AttributeSourceGroupEntity> users = userRoot.join("groups");
	    CriteriaQuery<AttributeSourceGroupEntity> cq = criteria.select(users);
	    TypedQuery<AttributeSourceGroupEntity> query = em.createQuery(cq);
	    return query.getResultList();		
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
