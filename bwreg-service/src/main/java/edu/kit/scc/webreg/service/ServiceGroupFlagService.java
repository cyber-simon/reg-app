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

import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;

public interface ServiceGroupFlagService extends BaseService<ServiceGroupFlagEntity, Long> {

	List<ServiceGroupFlagEntity> findByGroup(ServiceBasedGroupEntity group);

	List<ServiceGroupFlagEntity> findByGroupAndService(
			ServiceBasedGroupEntity group, ServiceEntity service);

	List<ServiceGroupFlagEntity> findByService(ServiceEntity service);

	List<ServiceGroupFlagEntity> findLocalGroupsForService(ServiceEntity service);

	List<ServiceGroupFlagEntity> findByStatus(ServiceGroupStatus status);

	List<ServiceGroupFlagEntity> findByGroupAndStatus(
			ServiceBasedGroupEntity group, ServiceGroupStatus status);
	
}
