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

import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface LocalGroupService extends BaseService<LocalGroupEntity> {

	LocalGroupEntity save(LocalGroupEntity entity, ServiceEntity service);
	
	LocalGroupEntity createNew(ServiceEntity service);
	
	LocalGroupEntity findByName(String name);

	List<LocalGroupEntity> findByUser(UserEntity user);

	LocalGroupEntity findWithUsers(Long id);

	LocalGroupEntity findWithUsersAndChildren(Long id);

	void createServiceGroupFlagsBulk(ServiceEntity fromService, ServiceEntity toService, String filterRegex);
}
