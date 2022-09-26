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

import edu.kit.scc.webreg.entity.SamlUserEntity;

public interface SamlUserDao extends BaseDao<SamlUserEntity> {

	List<SamlUserEntity> findUsersForPseudo(Long onHoldSince, int limit);
	SamlUserEntity findByEppn(String eppn);
	SamlUserEntity findByIdWithStore(Long id);
	SamlUserEntity findByPersistent(String spId, String idpId, String persistentId);
	SamlUserEntity findByAttributeSourcedId(String spId, String idpId, String attributeSourcedIdName, String attributeSourcedId);
	SamlUserEntity findBySubject(String spId, String idpId, String subjectId);
}
