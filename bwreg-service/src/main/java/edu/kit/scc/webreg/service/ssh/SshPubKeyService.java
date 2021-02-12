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

import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.service.BaseService;
import edu.kit.scc.webreg.ssh.SshPubKeyBlacklistedException;

public interface SshPubKeyService extends BaseService<SshPubKeyEntity, Long> {

	SshPubKeyEntity deployKey(Long userId, SshPubKeyEntity entity, String executor)
			throws SshPubKeyBlacklistedException;

	SshPubKeyEntity deleteKey(SshPubKeyEntity entity, String executor);

	List<SshPubKeyEntity> findByKey(String encodedKey);

	List<SshPubKeyEntity> findByIdentity(Long identityId);

	List<SshPubKeyEntity> findByIdentityAndStatus(Long identityId, SshPubKeyStatus keyStatus);

	List<SshPubKeyEntity> findByIdentityAndStatusWithRegs(Long identityId, SshPubKeyStatus keyStatus);

	SshPubKeyEntity expireKey(SshPubKeyEntity entity, String executor);
	
	List<SshPubKeyEntity> findKeysToExpire(int limit);
}
