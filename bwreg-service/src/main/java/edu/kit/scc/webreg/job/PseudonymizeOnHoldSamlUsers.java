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

import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.service.saml.SamlUserDeprovisionService;


public class PseudonymizeOnHoldSamlUsers extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(PseudonymizeOnHoldSamlUsers.class);

		if (! getJobStore().containsKey("on_hold_since_millis")) {
			logger.warn("DeregisterInvalidRegistries Job is not configured correctly. invalid_since_millis Parameter is missing in JobMap");
			return;
		}

		Long lastUpdate = Long.parseLong(getJobStore().get("on_hold_since_millis"));
		int limit = 1;
		if (getJobStore().containsKey("limit")) {
			limit = Integer.parseInt(getJobStore().get("limit"));
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			SamlUserDeprovisionService service = (SamlUserDeprovisionService) ic.lookup("global/bwreg/bwreg-service/SamlUserDeprovisionServiceImpl!edu.kit.scc.webreg.service.saml.SamlUserDeprovisionService");
			List<SamlUserEntity> userList = service.findUsersForPseudo(lastUpdate, limit);
			
			logger.debug("Found {} users suitable for pseudonymisation", userList.size());
			
			for (SamlUserEntity user : userList) {
				logger.debug("Inspecting user {} - {} - {} - {} - {}", user.getId(), user.getEppn(), user.getEmail(), user.getUserStatus(), user.getLastStatusChange());
				service.pseudoUser(user);
			}
			
		} catch (NamingException e) {
			logger.warn("Could not pseudo saml users: {}", e);
		}
	}
}
