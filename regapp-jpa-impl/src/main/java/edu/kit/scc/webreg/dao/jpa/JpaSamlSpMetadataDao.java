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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.SamlSpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpMetadataEntity_;

@Named
@ApplicationScoped
public class JpaSamlSpMetadataDao extends JpaBaseDao<SamlSpMetadataEntity> implements SamlSpMetadataDao {

	@Override
	public List<SamlSpMetadataEntity> findAllByFederation(FederationEntity federation) {
		return em.createQuery("select distinct e from SamlSpMetadataEntity e join e.federations f where f = :fed",
				SamlSpMetadataEntity.class).setParameter("fed", federation).getResultList();
	}

	@Override
	public SamlSpMetadataEntity findByEntityId(String entityId) {
		List<SamlSpMetadataEntity> idps = findAll(equal(SamlSpMetadataEntity_.entityId, entityId));
		return idps.size() < 1 ? null : idps.get(0);
	}

	@Override
	public Class<SamlSpMetadataEntity> getEntityClass() {
		return SamlSpMetadataEntity.class;
	}

}
