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
package edu.kit.scc.webreg.event;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

public class GroupReconsiliationExecutor extends AbstractEventExecutor<MultipleGroupEvent, HashSet<GroupEntity>> {

	private static final long serialVersionUID = 1L;

	public GroupReconsiliationExecutor() {
		super();
	}

	@Override
	public void execute() {

		Logger logger = LoggerFactory.getLogger(GroupReconsiliationExecutor.class);
		logger.debug("Executing");

		String executor = getJobStore().get("executor");

		if (executor == null) {
			logger.warn("No executor configured for GroupReconsiliationExecutor. Using unknown");
			executor = "unknown";
		}

		Boolean reconRegistries = false;
		if (getJobStore().containsKey("recon_registries")) {
			reconRegistries = Boolean.parseBoolean(getJobStore().get("recon_registries"));
		}

		Boolean fullRecon = false;
		if (getJobStore().containsKey("full_recon")) {
			fullRecon = Boolean.parseBoolean(getJobStore().get("full_recon"));
		}

		Boolean newReconMethod = false;
		if (getJobStore().containsKey("new_recon")) {
			newReconMethod = Boolean.parseBoolean(getJobStore().get("new_recon"));
		}

		Boolean newRollMech = false;
		if (getJobStore().containsKey("new_roll")) {
			newRollMech = Boolean.parseBoolean(getJobStore().get("new_roll"));
		}

		Set<String> reconRegForServices = null;
		if (getJobStore().containsKey("recon_reg_for_services")) {
			String rs = getJobStore().get("recon_reg_for_services");
			reconRegForServices = new HashSet<String>(Arrays.asList(rs.split(";")));
		}

		try {
			InitialContext ic = new InitialContext();

			RegisterUserService registerUserService = (RegisterUserService) ic.lookup(
					"global/bwreg/bwreg-service/RegisterUserServiceImpl!edu.kit.scc.webreg.service.reg.RegisterUserService");

			Set<GroupEntity> groupList = getEvent().getEntity();
			Map<GroupEntity, Set<UserEntity>> usersToRemove = getEvent().getUsersToRemove();

			try {
				if (newReconMethod) {
					List<RegistryEntity> reconList = registerUserService.updateGroupsNew(groupList, reconRegistries,
							reconRegForServices, fullRecon, newRollMech, usersToRemove, executor);

					for (RegistryEntity registry : reconList) {
						try {
							registerUserService.reconsiliation(registry, fullRecon, executor);
						} catch (RegisterException e) {
							logger.warn("Could not recon registry", e);
						}
					}
				} else {
					registerUserService.updateGroups(groupList, reconRegistries, fullRecon, usersToRemove, executor);
				}
			} catch (RegisterException e) {
				logger.warn("Could not update groups ", e);
			}

		} catch (NamingException e) {
			logger.warn("Could execute: {}", e);
		}

	}
}
