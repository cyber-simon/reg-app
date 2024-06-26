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

import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
import edu.kit.scc.webreg.service.identity.IdentityEmailAddressService;


public class ExpireVerifiedEmailAddresses extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ExpireVerifiedEmailAddresses.class);

		try {
			logger.debug("Expire verified email addresses");

			Integer limit;
			
			if (getJobStore().containsKey("limit")) {
				limit = Integer.parseInt(getJobStore().get("limit"));
			}
			else {
				limit = 1;
			}

			InitialContext ic = new InitialContext();
			
			IdentityEmailAddressService service = (IdentityEmailAddressService) ic.lookup("java:global/bwreg/bwreg-service/IdentityEmailAddressService!edu.kit.scc.webreg.service.identity.IdentityEmailAddressService");
			
			List<IdentityEmailAddressEntity> emailList = service.findVerifiedToExpire(limit);
			
			for (IdentityEmailAddressEntity email : emailList) {
				service.expireEmailAddress(email, "ExpireEmailAddress-job");
			}
			
			logger.debug("Expire done");
			
		} catch (NamingException e) {
			logger.warn("Could not expire verified email addresses: {}", e);
		}
	}
}
