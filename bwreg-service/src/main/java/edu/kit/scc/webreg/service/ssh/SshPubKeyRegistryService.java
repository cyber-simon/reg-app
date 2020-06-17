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
package edu.kit.scc.webreg.service.ssh;

import java.util.List;

import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.service.BaseService;

public interface SshPubKeyRegistryService extends BaseService<SshPubKeyRegistryEntity, Long> {

	List<SshPubKeyRegistryEntity> findByUserAndService(Long userId, Long serviceId);

	List<SshPubKeyRegistryEntity> findByRegistry(Long registryId);

	SshPubKeyRegistryEntity deployRegistry(SshPubKeyRegistryEntity entity, String executor);

	void deleteRegistry(SshPubKeyRegistryEntity entity, String executor);

}
