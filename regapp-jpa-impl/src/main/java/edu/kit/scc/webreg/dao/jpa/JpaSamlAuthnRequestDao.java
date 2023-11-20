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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.persistence.Query;

import edu.kit.scc.webreg.dao.SamlAuthnRequestDao;
import edu.kit.scc.webreg.entity.SamlAuthnRequestEntity;

@Named
@ApplicationScoped
public class JpaSamlAuthnRequestDao extends JpaBaseDao<SamlAuthnRequestEntity> implements SamlAuthnRequestDao {

	@Override
	public void deleteInvalid() {
		Query query = em.createQuery("delete from SamlAuthnRequestEntity where validUntil <= :validUntil");
		query.setParameter("validUntil", new Date());
		query.executeUpdate();
	}

	@Override
	public Class<SamlAuthnRequestEntity> getEntityClass() {
		return SamlAuthnRequestEntity.class;
	}
}
