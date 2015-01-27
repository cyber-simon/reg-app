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

import edu.kit.scc.webreg.dao.EmailTemplateDao;
import edu.kit.scc.webreg.entity.EmailTemplateEntity;

@Named
@ApplicationScoped
public class JpaEmailTemplateDao extends JpaBaseDao<EmailTemplateEntity, Long> implements EmailTemplateDao {

    @Override
	public EmailTemplateEntity findByName(String name) {
		try {
			return (EmailTemplateEntity) em.createQuery("select e from EmailTemplateEntity e where e.name = :name")
				.setParameter("name", name).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public Class<EmailTemplateEntity> getEntityClass() {
		return EmailTemplateEntity.class;
	}
}
