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

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.service.reg.ApprovalService;

public class AutoApprovalExecutor extends
		AbstractEventExecutor<ServiceRegisterEvent, Long> {

	private static final long serialVersionUID = 1L;

	public AutoApprovalExecutor() {
		super();
	}

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(AutoApprovalExecutor.class);
		logger.debug("Executing");
		
		try {
			InitialContext ic = new InitialContext();
			
			RegistryService registryService = (RegistryService) ic.lookup("global/bwreg/bwreg-service/RegistryServiceImpl!edu.kit.scc.webreg.service.RegistryService");
			RegistryEntity registry = registryService.fetch(getEvent().getEntity());

			logger.info("Checking autoapproval for user {}", registry.getUser().getEppn());

			KnowledgeSessionService knowledgeSessionService = (KnowledgeSessionService) ic.lookup("global/bwreg/bwreg-service/KnowledgeSessionServiceImpl!edu.kit.scc.webreg.service.drools.KnowledgeSessionService");

			List<Object> objectList = knowledgeSessionService.checkServiceAccessRule(
					registry.getUser(), registry.getService(), registry, "autoapproval", false);

			List<String> requirementsList = new ArrayList<String>();
			for (Object o : objectList) {
				if (o instanceof OverrideAccess) {
					requirementsList.clear();
					logger.debug("Removing requirements due to OverrideAccess");
					break;
				}
				else if (o instanceof UnauthorizedUser) {
					String s = ((UnauthorizedUser) o).getMessage();
					requirementsList.add(s);
				}
			}

			if (requirementsList.size() == 0) {
				ApprovalService approvalService = (ApprovalService) ic.lookup("global/bwreg/bwreg-service/ApprovalServiceImpl!edu.kit.scc.webreg.service.reg.ApprovalService");
	
				try {
					approvalService.approve(registry, "autoappoval", null);
				} catch (RegisterException e) {
					logger.warn("Could not complete autoapproval", e);
				}
			}
			else {
				logger.info("User {} must be manually approved", registry.getUser().getEppn());
			}
			
		} catch (NamingException e) {
			logger.warn("Could not autoapprove: {}", e);
		}
		
	}

}
