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

import edu.kit.scc.webreg.entity.FederationEntity;
import edu.kit.scc.webreg.service.FederationService;


public class UpdateAllFederationMetadata extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(UpdateAllFederationMetadata.class);

		try {
			InitialContext ic = new InitialContext();
			
			FederationService federationService = (FederationService) ic.lookup("global/bwreg/bwreg-service/FederationServiceImpl!edu.kit.scc.webreg.service.FederationService");
			
			List<FederationEntity> federationList = federationService.findAll();
			
			for (FederationEntity federation : federationList) {
				logger.debug("Updateing federation {}", federation.getEntityId());
				federationService.updateFederation(federation);
			}
		} catch (NamingException e) {
			logger.warn("Could not update Federation Metadata: {}", e);
		}
	}
}
