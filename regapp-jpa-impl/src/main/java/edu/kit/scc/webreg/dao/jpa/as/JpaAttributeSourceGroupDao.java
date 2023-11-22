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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.as.AttributeSourceGroupDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceGroupEntity;

@Named
@ApplicationScoped
public class JpaAttributeSourceGroupDao extends JpaBaseDao<AttributeSourceGroupEntity>
		implements AttributeSourceGroupDao {

	@Override
	public List<AttributeSourceGroupEntity> findByUserAndAS(UserEntity user, AttributeSourceEntity attributeSource) {
		return em.createQuery(
				"select e from AttributeSourceGroupEntity e left join e.users as ug where ug.user = :user and e.attributeSource = :attributeSource",
				AttributeSourceGroupEntity.class).setParameter("user", user)
				.setParameter("attributeSource", attributeSource).getResultList();
	}

	@Override
	public Class<AttributeSourceGroupEntity> getEntityClass() {
		return AttributeSourceGroupEntity.class;
	}

}
