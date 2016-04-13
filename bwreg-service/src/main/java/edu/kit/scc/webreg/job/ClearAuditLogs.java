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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.audit.AuditEntryService;


public class ClearAuditLogs extends AbstractExecutableJob {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ClearAuditLogs.class);

		if (! getJobStore().containsKey("purge_millis")) {
			logger.warn("ClearAuditLogs Job is not configured correctly. purge_millis Parameter is missing in JobMap");
			return;
		}
		
		Long purgeMillis = Long.parseLong(getJobStore().get("purge_millis"));

		Integer limit;

		if (! getJobStore().containsKey("limit")) {
			limit = 50;
		}
		else {
			limit = Integer.parseInt(getJobStore().get("limit"));
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			AuditEntryService auditEntryService = (AuditEntryService) ic.lookup("global/bwreg/bwreg-service/AuditEntryServiceImpl!edu.kit.scc.webreg.audit.AuditEntryService");
			
			auditEntryService.deleteAllOlderThan(new Date(System.currentTimeMillis() - purgeMillis), limit);
			
			logger.debug("Deletion done");
			
		} catch (NamingException e) {
			logger.warn("Could not clear Audit Logs: {}", e);
		}
	}
}
