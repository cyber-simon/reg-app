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

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.regapp.mail.api.TemplateMailService;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;

public class ProjectServiceSendMailExecutor extends
		AbstractEventExecutor<ProjectServiceEvent, ProjectServiceEntity> {

	private static final long serialVersionUID = 1L;

	public ProjectServiceSendMailExecutor() {
		super();
	}

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ProjectServiceSendMailExecutor.class);
		logger.debug("Executing");
		
		String templateName = getJobStore().get("mail_template");

		if (templateName == null) {
			logger.warn("No template configured for ProjectServiceSendMailExecutor");
			return;
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			TemplateMailService templateMailService = (TemplateMailService) ic.lookup("global/bwreg/bwreg-service/TemplateMailServiceImpl!edu.kit.scc.regapp.mail.api.TemplateMailService");
			
			ProjectServiceEntity pse = getEvent().getEntity();
			Map<String, Object> context = new HashMap<String, Object>(3);
			context.put("pse", pse);
			context.put("project", pse.getProject());
			context.put("service", pse.getService());
			
			templateMailService.sendMail(templateName, context, true);
			
		} catch (NamingException e) {
			logger.warn("Could not send email: {}", e);
		}
		
	}

}
