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

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.RegistryService;

public class UserUpdateAccessCheckExecutor extends
		AbstractEventExecutor<UserEvent, UserEntity> {

	private static final long serialVersionUID = 1L;

	public UserUpdateAccessCheckExecutor() {
		super();
	}

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(UserUpdateAccessCheckExecutor.class);
		logger.debug("Executing");
	
		String executor = getJobStore().get("executor");
		if (executor == null) {
			logger.warn("No executor configured for GroupReconsiliationExecutor. Using unknown");
			executor = "unknown";
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			KnowledgeSessionService knowledgeSessionService = (KnowledgeSessionService) ic.lookup("global/bwreg/bwreg-service/KnowledgeSessionServiceImpl!edu.kit.scc.webreg.drools.KnowledgeSessionService");
			RegistryService registryService = (RegistryService) ic.lookup("global/bwreg/bwreg-service/RegistryServiceImpl!edu.kit.scc.webreg.service.RegistryService");
			
			UserEntity user = getEvent().getEntity();
			List<RegistryEntity> registryList = registryService.findByUserAndStatus(user, RegistryStatus.ACTIVE);
			
			knowledgeSessionService.checkRules(registryList, user, executor);
			
		} catch (NamingException e) {
			logger.warn("Could not check access: {}", e);
		}
		
	}

}
