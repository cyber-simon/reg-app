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

import java.util.Date;
import java.util.List;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

public interface RegistryService extends BaseService<RegistryEntity> {

	List<RegistryEntity> findByService(ServiceEntity service);

	List<RegistryEntity> findByServiceAndStatus(ServiceEntity service, RegistryStatus status);

	List<RegistryEntity> findByIdentityAndStatus(IdentityEntity identity, RegistryStatus... status);

	RegistryEntity findByServiceAndUserAndStatus(ServiceEntity service, UserEntity user, RegistryStatus status);

	RegistryEntity findByIdWithAgreements(Long id);

	List<RegistryEntity> findByUser(UserEntity user);

	List<RegistryEntity> findByIdentityAndNotStatusAndNotHidden(IdentityEntity identity, RegistryStatus... status);

	List<RegistryEntity> findRegistriesForDepro(String serviceShortName);

	List<RegistryEntity> findByServiceAndNotStatus(ServiceEntity service, RegistryStatus... status);

	List<RegistryEntity> findByServiceAndStatusAndIDPGood(String serviceShortName, RegistryStatus status, Date date,
			int limit);

	List<RegistryEntity> findByServiceOrderByRecon(ServiceEntity service, int limit);

	List<RegistryEntity> findByServiceAndStatusOrderByRecon(ServiceEntity service, RegistryStatus status, int limit);

	List<RegistryEntity> findByServiceAndIdentityAndNotStatus(ServiceEntity service, IdentityEntity identity,
			RegistryStatus... status);

	List<RegistryEntity> findByIdentity(IdentityEntity identity);

	List<RegistryEntity> findByUserAndStatus(UserEntity user, RegistryStatus... status);
}
