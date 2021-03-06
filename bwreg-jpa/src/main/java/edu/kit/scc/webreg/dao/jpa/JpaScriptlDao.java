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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.entity.ScriptEntity;

@Named
@ApplicationScoped
public class JpaScriptlDao extends JpaBaseDao<ScriptEntity> implements ScriptDao {

    @Override
	public ScriptEntity findByName(String name) {
		try {
			return (ScriptEntity) em.createQuery("select e from ScriptEntity e where e.name = :name")
				.setParameter("name", name).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Class<ScriptEntity> getEntityClass() {
		return ScriptEntity.class;
	}
}
