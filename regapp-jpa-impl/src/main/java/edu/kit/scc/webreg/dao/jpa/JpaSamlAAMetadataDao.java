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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.SamlAAMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlAAConfigurationEntity_;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;

@Named
@ApplicationScoped
public class JpaSamlAAMetadataDao extends JpaBaseDao<SamlAAMetadataEntity> implements SamlAAMetadataDao {

	@Override
	public List<SamlAAMetadataEntity> findAllByFederation(FederationEntity federation) {
		return em.createQuery("select distinct e from SamlAAMetadataEntity e join e.federations f where f = :fed",
				SamlAAMetadataEntity.class).setParameter("fed", federation).getResultList();
	}

	@Override
	public SamlAAMetadataEntity findByEntityId(String entityId) {
		List<SamlAAMetadataEntity> idps = findAll(equal(SamlAAConfigurationEntity_.entityId, entityId));
		return (idps.size() < 1) ? null : idps.get(0);
	}

	@Override
	public Class<SamlAAMetadataEntity> getEntityClass() {
		return SamlAAMetadataEntity.class;
	}

}
