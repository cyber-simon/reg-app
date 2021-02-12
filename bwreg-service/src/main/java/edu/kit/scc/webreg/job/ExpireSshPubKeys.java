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
package edu.kit.scc.webreg.job;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.service.ssh.SshPubKeyService;


public class ExpireSshPubKeys extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ExpireSshPubKeys.class);

		try {
			logger.debug("Expire Ssh pub keys");

			Integer limit;
			
			if (getJobStore().containsKey("limit")) {
				limit = Integer.parseInt(getJobStore().get("limit"));
			}
			else {
				limit = 1;
			}

			InitialContext ic = new InitialContext();
			
			SshPubKeyService service = (SshPubKeyService) ic.lookup("global/bwreg/bwreg-service/SshPubKeyServiceImpl!edu.kit.scc.webreg.service.ssh.SshPubKeyService");
			
			List<SshPubKeyEntity> keyList = service.findKeysToExpire(limit);
			
			for (SshPubKeyEntity key : keyList) {
				service.expireKey(key, "ExpireSshPubKeys-job");
			}
			
			logger.debug("Expire done");
			
		} catch (NamingException e) {
			logger.warn("Could not expire SSH Keys: {}", e);
		}
	}
}
