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

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.SamlIdpScopeDao;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpScopeEntity;

@Named
@ApplicationScoped
public class JpaSamlIdpScopeDao extends JpaBaseDao<SamlIdpScopeEntity, Long> implements SamlIdpScopeDao {

    @Override
    @SuppressWarnings({"unchecked"})
	public List<SamlIdpScopeEntity> findByIdp(SamlIdpMetadataEntity idp) {
		return em.createQuery("select e from SamlIdpScopeEntity e where e.idp = :idp")
				.setParameter("idp", idp).getResultList();
	}

	@Override
	public Class<SamlIdpScopeEntity> getEntityClass() {
		return SamlIdpScopeEntity.class;
	}
}
