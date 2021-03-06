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

import edu.kit.scc.webreg.service.UserLoginInfoService;


public class ClearExpiredLoginInfo extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ClearExpiredLoginInfo.class);

		try {
			logger.debug("Delete expired Login Infos");

			long purgeMillis;

			if (! getJobStore().containsKey("purge_millis")) {
				purgeMillis = 7776000000L; // 90 days default
			}
			else {
				purgeMillis = Long.parseLong(getJobStore().get("purge_millis"));
			}

			InitialContext ic = new InitialContext();
			
			UserLoginInfoService service = (UserLoginInfoService) ic.lookup("global/bwreg/bwreg-service/UserLoginInfoServiceImpl!edu.kit.scc.webreg.service.UserLoginInfoService");
			
			service.deleteLoginInfo(purgeMillis);
			
			logger.debug("Deletion done");
			
		} catch (NamingException e) {
			logger.warn("Could not delete Login Infos: {}", e);
		}
	}
}
