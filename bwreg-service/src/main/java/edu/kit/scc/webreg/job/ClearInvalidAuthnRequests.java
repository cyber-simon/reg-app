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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.service.SamlAuthnRequestService;


public class ClearInvalidAuthnRequests extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ClearInvalidAuthnRequests.class);

		try {
			logger.debug("Delete expired AuthnRequests");

			InitialContext ic = new InitialContext();
			
			SamlAuthnRequestService service = (SamlAuthnRequestService) ic.lookup("global/bwreg/bwreg-service/SamlAuthnRequestServiceImpl!edu.kit.scc.webreg.service.SamlAuthnRequest");
			
			service.deleteInvalid();
			
			logger.debug("Deletion done");
			
		} catch (NamingException e) {
			logger.warn("Could not delete expired AuthnRequests: {}", e);
		}
	}
}
