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

import edu.kit.scc.webreg.dao.TextPropertyDao;
import edu.kit.scc.webreg.entity.TextPropertyEntity;

@Named
@ApplicationScoped
public class JpaTextPropertyDao extends JpaBaseDao<TextPropertyEntity, Long> implements TextPropertyDao {

    @Override
	public TextPropertyEntity findByKeyAndLang(String key, String language) {
		try {
			return (TextPropertyEntity) em.createQuery("select e from TextPropertyEntity e "
					+ "where e.key = :key and e.language = :language")
				.setParameter("key", key)
				.setParameter("language", language)
				.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}


	
	@Override
	public Class<TextPropertyEntity> getEntityClass() {
		return TextPropertyEntity.class;
	}
}
