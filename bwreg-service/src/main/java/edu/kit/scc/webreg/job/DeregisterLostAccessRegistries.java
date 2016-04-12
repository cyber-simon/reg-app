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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;


public class DeregisterLostAccessRegistries extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(DeregisterLostAccessRegistries.class);

		if (! getJobStore().containsKey("lost_access_since_millis")) {
			logger.warn("DeregisterLostAccessRegistries Job is not configured correctly. lost_access_since_millis Parameter is missing in JobMap");
			return;
		}
		if (! getJobStore().containsKey("service_short_name")) {
			logger.warn("DeregisterLostAccessRegistries Job is not configured correctly. service_short_name Parameter is missing in JobMap");
			return;
		}

		Long lastUpdate = Long.parseLong(getJobStore().get("lost_access_since_millis"));
		String ssn = getJobStore().get("service_short_name");
		
		Long lastUserUpdate =  14 * 24 * 60 * 60 * 1000L; // 14 days standard value
		if (getJobStore().containsKey("last_user_update_millis")) 
			lastUserUpdate = Long.parseLong(getJobStore().get("last_user_update_millis"));
		
		Integer limit = 1;
		if (getJobStore().containsKey("registries_per_exec"))
			limit = Integer.parseInt(getJobStore().get("registries_per_exec"));

		logger.info("Starting Deregister LostAccess Registries for {}", ssn);
		
		try {
			InitialContext ic = new InitialContext();
			
			RegistryService registryService = (RegistryService) ic.lookup("global/bwreg/bwreg-service/RegistryServiceImpl!edu.kit.scc.webreg.service.RegistryService");
			RegisterUserService registerUserService = (RegisterUserService) ic.lookup("global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");

			UserService userService = (UserService) ic.lookup("global/bwreg/bwreg-service/UserServiceImpl!edu.kit.scc.webreg.service.UserService");
			KnowledgeSessionService knowledgeSessionService = (KnowledgeSessionService) ic.lookup("global/bwreg/bwreg-service/KnowledgeSessionServiceImpl!edu.kit.scc.webreg.drools.KnowledgeSessionService");
			
			List<RegistryEntity> registryList = registryService.findByServiceAndStatus(ssn, RegistryStatus.LOST_ACCESS, 
					new Date(System.currentTimeMillis() - lastUpdate), limit);
			
			if (registryList.size() == 0)
				logger.debug("No LostAccess registries found");
			
			for (RegistryEntity registry : registryList) {
				try {
					logger.info("Deregister Registry {}", registry.getId());
					UserEntity user = registry.getUser();
					if ((System.currentTimeMillis() - user.getLastUpdate().getTime()) > lastUserUpdate) {
						// user is too old, try update first
						logger.info("User {} lastUpdate is older than {}ms. Trying update", user.getEppn(), lastUserUpdate);
						try {
							userService.updateUserFromIdp(user, "lost-access-reg-job");
						} catch (UserUpdateException e) {
							logger.info("Exception while Querying IDP: {}", e.getMessage());
							if (e.getCause() != null) {
								logger.info("Cause is: {}", e.getCause().getMessage());
								if (e.getCause().getCause() != null) {
									logger.info("Inner Cause is: {}", e.getCause().getCause().getMessage());
								}
							}
						}
						List<RegistryEntity> tempRegistryList = new ArrayList<RegistryEntity>();
						tempRegistryList.add(registry);
						knowledgeSessionService.checkRules(tempRegistryList, user, "lost-access-reg-job", false);
					}
					
					if (RegistryStatus.LOST_ACCESS.equals(registry.getRegistryStatus())) {
						registerUserService.deregisterUser(registry, "lost-access-reg-job");
					}
				} catch (RegisterException e) {
					logger.info("Could not deregister", e);
				}
			}
			
		} catch (NamingException e) {
			logger.warn("Could not Deregister LostAccess Registries: {}", e);
		}
	}
}
