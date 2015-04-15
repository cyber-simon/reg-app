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

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.ApplicationConfigDao;
import edu.kit.scc.webreg.entity.ApplicationConfigEntity;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;

@Named
@ApplicationScoped
public class JpaApplicationConfigDao extends JpaBaseDao<ApplicationConfigEntity, Long> implements ApplicationConfigDao {

    @Override
	public ApplicationConfigEntity findActive() {
		try {
			return (ApplicationConfigEntity) em.createQuery("select e from ApplicationConfigEntity e where e.activeConfig = :act")
					.setParameter("act", true)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

    @Override
    @SuppressWarnings({"unchecked"})
	public ApplicationConfigEntity findReloadActive(Date date) {
		try {
			return (ApplicationConfigEntity) em.createQuery("select e from ApplicationConfigEntity e "
				+ "where e.activeConfig = :act and e.dirtyStamp > :date")
				.setParameter("date", date).setParameter("act", true)
				.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}
    
	@Override
	public Class<ApplicationConfigEntity> getEntityClass() {
		return ApplicationConfigEntity.class;
	}
}
