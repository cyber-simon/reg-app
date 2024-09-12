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
import edu.kit.scc.webreg.service.UserService;


public class UserExpire extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(UserExpire.class);

		Integer limit, days;
		String emailTemplateName = "user_expiry";

		if (getJobStore().containsKey("user_expiry_template")) {
			emailTemplateName = getJobStore().get("user_expiry_template");
		}
		
		if (getJobStore().containsKey("limit")) {
			limit = Integer.parseInt(getJobStore().get("limit"));
		}
		else {
			limit = 1;
		}

		if (getJobStore().containsKey("days")) {
			days = Integer.parseInt(getJobStore().get("days"));
		}
		else {
			days = 14;
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			UserService service = (UserService) ic.lookup("global/bwreg/bwreg-service/UserServiceImpl!edu.kit.scc.webreg.service.UserService");
			List<UserEntity> userList = service.findUsersForExpiry(limit, days);
			
			logger.debug("Found {} users suitable for expiry", userList.size());
			
			for (UserEntity user : userList) {
				logger.debug("Inspecting user {} - {} - {} - {} - {}", user.getId(), user.getEppn(), user.getEmail(), user.getUserStatus(), user.getLastStatusChange());
				service.expireUser(user, emailTemplateName);
			}
			
		} catch (NamingException e) {
			logger.warn("Could not expire users: {}", e);
		}
	}
}
