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

import edu.kit.scc.webreg.dao.as.ASUserAttrValueDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueDateEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueLongEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrValueStringEntity;

@Named
@ApplicationScoped
public class JpaASUserAttrValueDao extends JpaBaseDao<ASUserAttrValueEntity, Long> implements ASUserAttrValueDao {

	@Override
	public ASUserAttrValueStringEntity createNewString() {
		return new ASUserAttrValueStringEntity();
	}
	
	@Override
	public ASUserAttrValueLongEntity createNewLong() {
		return new ASUserAttrValueLongEntity();
	}

	@Override
	public ASUserAttrValueDateEntity createNewDate() {
		return new ASUserAttrValueDateEntity();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ASUserAttrValueEntity> findValues(ASUserAttrEntity asUserAttr) {
		return (List<ASUserAttrValueEntity>) em.createQuery("select a from ASUserAttrValueEntity a where "
					+ "a.asUserAttr = :asUserAttr")
					.setParameter("asUserAttr", asUserAttr).getResultList();
	}
	
	@Override
	public ASUserAttrValueEntity findValueByKey(ASUserAttrEntity asUserAttr, String key) {
		try {
			return (ASUserAttrValueEntity) em.createQuery("select a from ASUserAttrValueEntity a where "
					+ "a.asUserAttr = :asUserAttr and a.key = :key")
					.setParameter("asUserAttr", asUserAttr).setParameter("key", key).getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public Class<ASUserAttrValueEntity> getEntityClass() {
		return ASUserAttrValueEntity.class;
	}
}
