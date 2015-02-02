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
package edu.kit.scc.webreg.service;

import java.util.List;

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.entity.SamlAAMetadataEntity;
import edu.kit.scc.webreg.entity.SamlMetadataEntityStatus;

public interface SamlAAMetadataService extends BaseService<SamlAAMetadataEntity, Long> {

	List<SamlAAMetadataEntity> findAllByFederation(FederationEntity federation);

	List<SamlAAMetadataEntity> findAllByFederationOrderByOrgname(
			FederationEntity federation);

	SamlAAMetadataEntity findByEntityId(String entityId);

	SamlAAMetadataEntity findByScope(String scope);

	SamlAAMetadataEntity findByIdWithAll(Long id);

	List<SamlAAMetadataEntity> findAllByStatusOrderedByOrgname(
			SamlMetadataEntityStatus status);

}
