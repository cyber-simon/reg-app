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

import edu.kit.scc.webreg.entity.ExternalUserAdminRoleEntity;
import edu.kit.scc.webreg.entity.ExternalUserEntity;

public interface ExternalUserDao extends BaseDao<ExternalUserEntity> {

	ExternalUserEntity findByExternalId(String externalId);
	List<ExternalUserEntity> findByAdmin(ExternalUserAdminRoleEntity adminRole);
	List<ExternalUserEntity> findByAttribute(String key, String value, ExternalUserAdminRoleEntity adminRole);
	List<ExternalUserEntity> findByGeneric(String key, String value, ExternalUserAdminRoleEntity adminRole);
	List<ExternalUserEntity> findAll(ExternalUserAdminRoleEntity adminRole);
	
}
