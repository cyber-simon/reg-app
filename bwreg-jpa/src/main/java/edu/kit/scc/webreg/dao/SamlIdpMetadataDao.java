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
package edu.kit.scc.webreg.dao;

import java.util.List;

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;

public interface SamlIdpMetadataDao extends BaseDao<SamlIdpMetadataEntity, Long> {

	List<SamlIdpMetadataEntity> findAllByFederation(FederationEntity federation);

	List<SamlIdpMetadataEntity> findAllByFederationOrderByOrgname(
			FederationEntity federation);

	SamlIdpMetadataEntity findByEntityId(String entityId);

	SamlIdpMetadataEntity findByScope(String scope);

	SamlIdpMetadataEntity findByIdWithAll(Long id);

	List<SamlIdpMetadataEntity> findAllByStatusOrderedByOrgname(
			SamlMetadataEntityStatus status);

}
