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

import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;

public interface SshPubKeyRegistryDao extends BaseDao<SshPubKeyRegistryEntity> {

	List<SshPubKeyRegistryEntity> findByUserAndService(Long userId, Long serviceId);

	List<SshPubKeyRegistryEntity> findByRegistry(Long registryId);

	List<SshPubKeyRegistryEntity> findForApproval(Long serviceId);

	List<SshPubKeyRegistryEntity> findByRegistryForInteractiveLogin(Long registryId);

	List<SshPubKeyRegistryEntity> findByRegistryForCommandLogin(Long registryId);

	List<SshPubKeyRegistryEntity> findByRegistryForLogin(Long registryId);

}
