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

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.identity.IdentityService;


public class SetPreferredUserOnIdentity extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(SetPreferredUserOnIdentity.class);

		logger.info("Starting SetPreferredUserOnIdentity Job");
		
		Integer limit;
		
		if (getJobStore().containsKey("users_per_exec")) {
			limit = Integer.parseInt(getJobStore().get("users_per_exec"));
		}
		else {
			limit = 1;
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			IdentityService identityService = (IdentityService) ic.lookup("global/bwreg/bwreg-service/IdentityServiceImpl!edu.kit.scc.webreg.service.identity.IdentityService");
			
			List<IdentityEntity> identityList = identityService.findByMissingPreferredUser(limit);
			
			logger.debug("Setting pref user for identities: {}", identityList.size());
			
			for (IdentityEntity identity : identityList) {
				logger.debug("Setting preferred User on identity {}", identity.getId());
				identityService.setPreferredUser(identity);
			}
			
		} catch (NamingException e) {
			logger.warn("Could not Update all Users from IDP: {}", e);
		}
	}
}
