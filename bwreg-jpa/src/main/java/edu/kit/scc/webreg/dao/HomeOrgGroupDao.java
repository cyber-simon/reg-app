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

import edu.kit.scc.webreg.entity.HomeOrgGroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface HomeOrgGroupDao extends BaseDao<HomeOrgGroupEntity, Long> {

	HomeOrgGroupEntity findByGidNumber(Integer gid);

	HomeOrgGroupEntity findByName(String name);

	HomeOrgGroupEntity findByNameAndPrefix(String name, String prefix);

	List<HomeOrgGroupEntity> findByUser(UserEntity user);

	HomeOrgGroupEntity findWithUsers(Long id);

	List<HomeOrgGroupEntity> findByNameListAndPrefix(List<String> nameList,
			String prefix);
}
