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

import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface LocalGroupDao extends BaseDao<LocalGroupEntity, Long> {

	LocalGroupEntity findByGidNumber(Integer gid);

	LocalGroupEntity findByName(String name);

	LocalGroupEntity findByNameAndPrefix(String name, String prefix);

	List<LocalGroupEntity> findByUser(UserEntity user);

	LocalGroupEntity findWithUsers(Long id);

	LocalGroupEntity findWithUsersAndChildren(Long id);

}
