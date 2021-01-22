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

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserUpdateFromHomeOrgService;


public class UpdateAllUserFromIdp extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(UpdateAllUserFromIdp.class);

		logger.info("Starting Update all Users from IDP Job");
				
		Integer limit;
		
		if (getJobStore().containsKey("users_per_exec")) {
			limit = Integer.parseInt(getJobStore().get("users_per_exec"));
		}
		else {
			limit = 1;
		}
		
		logger.debug("Update Users from IDP");
		
		try {
			InitialContext ic = new InitialContext();
			
			UserUpdateFromHomeOrgService updater = (UserUpdateFromHomeOrgService) ic.lookup("global/bwreg/bwreg-service/UserUpdateFromHomeOrgServiceImpl!edu.kit.scc.webreg.service.UserUpdateFromHomeOrgService");

			List<UserEntity> userList = updater.findScheduledUsers(limit);
			
			logger.debug("Updating scheduled users: {}", userList.size());
			
			for (UserEntity user : userList) {
				logger.info("Async updating user {}", user.getEppn());
				updater.updateUserAsync(user, "update-all-users-from-idp-job");
			}
		} catch (NamingException e) {
			logger.warn("Could not Update all Users from IDP: {}", e);
		}
	}
}
