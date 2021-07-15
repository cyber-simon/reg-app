/*******************************************************************************
 * Copyright (c) 2021 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.dao.oidc;

import java.util.List;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcGroupEntity;

public interface OidcGroupDao extends BaseDao<OidcGroupEntity, Long> {

	OidcGroupEntity findByGidNumber(Integer gid);

	OidcGroupEntity findByName(String name);

	OidcGroupEntity findByNameAndPrefix(String name, String prefix);

	List<OidcGroupEntity> findByUser(UserEntity user);

	OidcGroupEntity findWithUsers(Long id);

	List<OidcGroupEntity> findByNameListAndPrefix(List<String> nameList,
			String prefix);
}
