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

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.Query;

import edu.kit.scc.webreg.dao.SamlAssertionDao;
import edu.kit.scc.webreg.entity.SamlAssertionEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@Named
@ApplicationScoped
public class JpaSamlAssertionDao extends JpaBaseDao<SamlAssertionEntity, Long> implements SamlAssertionDao, Serializable {

	private static final long serialVersionUID = 1L;
    
	@Override
	public Class<SamlAssertionEntity> getEntityClass() {
		return SamlAssertionEntity.class;
	}
	
	@Override
	public void deleteAssertionForUser(UserEntity user) {
		Query query = em.createQuery("delete from SamlAssertionEntity where user=:user");
		query.setParameter("user", user);
		query.executeUpdate();
	}
}
