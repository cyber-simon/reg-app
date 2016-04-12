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

import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserService;


public class UpdateAllUserFromIdp extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(UpdateAllUserFromIdp.class);

		logger.info("Starting Update all Users from IDP Job");
		
		if (! getJobStore().containsKey("last_update_older_than_millis")) {
			logger.warn("ReconServiceRegistry Job is not configured correctly. last_update_older_than_millis Parameter is missing in JobMap");
			return;
		}

		Long lastUpdate = Long.parseLong(getJobStore().get("last_update_older_than_millis"));
		
		if (! getJobStore().containsKey("last_failed_update_older_than_millis")) {
			logger.warn("ReconServiceRegistry Job is not configured correctly. last_failed_update_older_than_millis Parameter is missing in JobMap");
			return;
		}

		Long lastFailedUpdate = Long.parseLong(getJobStore().get("last_failed_update_older_than_millis"));
		
		Integer limit;
		
		if (getJobStore().containsKey("users_per_exec")) {
			limit = Integer.parseInt(getJobStore().get("users_per_exec"));
		}
		else {
			limit = 1;
		}
		
		logger.debug("Update all Users from IDP if older than {} ms", lastUpdate);
		
		try {
			InitialContext ic = new InitialContext();
			
			UserService userService = (UserService) ic.lookup("global/bwreg/bwreg-service/UserServiceImpl!edu.kit.scc.webreg.service.UserService");
			
			List<UserEntity> userList = userService.findOrderByFailedUpdateWithLimit(new Date(System.currentTimeMillis() - lastFailedUpdate), limit);
			
			logger.debug("Updating user whith failed IDP communication attemp: {}", userList.size());
			
			if (userList.size() < limit) {
				userList.addAll(userService.findOrderByUpdatedWithLimit(new Date(System.currentTimeMillis() - lastUpdate), limit));
			}

			logger.debug("Updating {} users", userList.size());

			for (UserEntity user : userList) {
				try {
					logger.info("Updating user {}", user.getEppn());
					userService.updateUserFromIdp(user, "update-all-users-from-idp-job");
				} catch (UserUpdateException e) {
					logger.warn("Could not update user {}: {}", user.getEppn(), e);
				}
			}
			
		} catch (NamingException e) {
			logger.warn("Could not Update all Users from IDP: {}", e);
		}
	}
}
