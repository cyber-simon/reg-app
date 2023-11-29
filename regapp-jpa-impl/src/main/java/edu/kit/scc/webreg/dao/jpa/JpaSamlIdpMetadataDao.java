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

import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity_;

@Named
@ApplicationScoped
public class JpaSamlIdpMetadataDao extends JpaBaseDao<SamlIdpMetadataEntity> implements SamlIdpMetadataDao {

	@Override
	public SamlIdpMetadataEntity findByIdWithAll(Long id) {
		return find(equal(SamlIdpMetadataEntity_.id, id), SamlIdpMetadataEntity_.scopes,
				SamlIdpMetadataEntity_.genericStore, SamlIdpMetadataEntity_.federations);
	}

	@Override
	public List<SamlIdpMetadataEntity> findAllByFederation(FederationEntity federation) {
		return em.createQuery("select distinct e from SamlIdpMetadataEntity e join e.federations f where f = :fed",
				SamlIdpMetadataEntity.class).setParameter("fed", federation).getResultList();
	}

	@Override
	public SamlIdpMetadataEntity findByEntityId(String entityId) {
		List<SamlIdpMetadataEntity> idps = findAll(equal(SamlIdpMetadataEntity_.entityId, entityId));
		return (idps.size() < 1) ? null : idps.get(0);
	}

	@Override
	public SamlIdpMetadataEntity findByScope(String scope) {
		List<SamlIdpMetadataEntity> idpList = em
				.createQuery("select e from SamlIdpMetadataEntity as e join e.scopes as s where s.scope = :scope",
						SamlIdpMetadataEntity.class)
				.setParameter("scope", scope).getResultList();
		return idpList.size() == 0 ? null : idpList.get(0);
	}

	@Override
	public List<SamlIdpMetadataEntity> findAllByFederationOrderByOrgname(FederationEntity federation) {
		return em.createQuery(
				"select distinct e from SamlIdpMetadataEntity e join e.federations f left join fetch e.entityCategoryList where f = :fed order by e.orgName asc",
				SamlIdpMetadataEntity.class).setParameter("fed", federation).getResultList();
	}

	@Override
	public Class<SamlIdpMetadataEntity> getEntityClass() {
		return SamlIdpMetadataEntity.class;
	}

}
