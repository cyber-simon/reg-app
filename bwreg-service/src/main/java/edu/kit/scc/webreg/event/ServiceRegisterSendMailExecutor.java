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

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.service.mail.TemplateMailService;

public class ServiceRegisterSendMailExecutor extends
		AbstractEventExecutor<ServiceRegisterEvent, RegistryEntity> {

	private static final long serialVersionUID = 1L;

	public ServiceRegisterSendMailExecutor() {
		super();
	}

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(ServiceRegisterSendMailExecutor.class);
		logger.debug("Executing");
		
		String templateName = getJobStore().get("mail_template");

		if (templateName == null) {
			logger.warn("No template configured for ServiceRegisterSendMailExecutor");
			return;
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			TemplateMailService templateMailService = (TemplateMailService) ic.lookup("global/bwreg/bwreg-service/TemplateMailServiceImpl!edu.kit.scc.webreg.service.mail.TemplateMailService");
			
			RegistryEntity registry = getEvent().getEntity();
			Map<String, Object> context = new HashMap<String, Object>(3);
			context.put("registry", registry);
			context.put("user", registry.getUser());
			context.put("service", registry.getService());
			
			templateMailService.sendMail(templateName, context, true);
			
		} catch (NamingException e) {
			logger.warn("Could not send email: {}", e);
		}
		
	}

}
