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

import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;

public interface RegistryDao extends BaseDao<RegistryEntity, Long> {

	List<RegistryEntity> findByService(ServiceEntity service);

	List<RegistryEntity> findAllByStatus(RegistryStatus status);

	List<RegistryEntity> findByServiceAndStatus(ServiceEntity service,
			RegistryStatus status);

	List<RegistryEntity> findByUserAndStatus(UserEntity user,
			RegistryStatus... status);

	List<RegistryEntity> findByServiceAndUser(ServiceEntity service, UserEntity user);

	RegistryEntity findByServiceAndUserAndStatus(ServiceEntity service,
			UserEntity user, RegistryStatus status);

	RegistryEntity findByIdWithAgreements(Long id);

	List<RegistryEntity> findByServiceAndStatusPaging(ServiceEntity service,
			RegistryStatus status, int first, int pageSize, String sortField,
			GenericSortOrder sortOrder, Map<String, Object> filterMap);

	Number countServiceAndStatus(ServiceEntity service, RegistryStatus status,
			Map<String, Object> filterMap);

	List<RegistryEntity> findByUser(UserEntity user);

	List<UserEntity> findUserListByServiceAndStatus(ServiceEntity service,
			RegistryStatus status);

	List<RegistryEntity> findByServiceAndStatus(String serviceShortName,
			RegistryStatus status, Date date, int limit);

	List<RegistryEntity> findByServiceAndUserAndNotStatus(ServiceEntity service,
			UserEntity user, RegistryStatus... status);

	List<RegistryEntity> findRegistriesForDepro(String serviceShortName);

	List<RegistryEntity> findByUserAndNotStatusAndNotHidden(UserEntity user,
			RegistryStatus... status);

	List<RegistryEntity> findByServiceAndNotStatus(ServiceEntity service,
			RegistryStatus... status);

	List<RegistryEntity> findByServiceAndStatusAndIDPGood(String serviceShortName, RegistryStatus status, Date date,
			int limit);

}
