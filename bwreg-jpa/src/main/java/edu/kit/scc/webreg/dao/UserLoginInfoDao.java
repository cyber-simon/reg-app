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

import edu.kit.scc.webreg.entity.UserLoginInfoEntity;
import edu.kit.scc.webreg.entity.UserLoginMethod;

public interface UserLoginInfoDao extends BaseDao<UserLoginInfoEntity, Long> {

	List<UserLoginInfoEntity> findByUser(Long userId);

	List<UserLoginInfoEntity> findByRegistry(Long registryId);

	UserLoginInfoEntity findLastByRegistryAndMethod(Long registryId, UserLoginMethod method);

}
