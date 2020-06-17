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

import edu.kit.scc.webreg.entity.SshPubKeyRegistryEntity;
import edu.kit.scc.webreg.service.mail.TemplateMailService;

public class SshPubKeyRegistrySendMailExecutor extends
		AbstractEventExecutor<SshPubKeyRegistryEvent, SshPubKeyRegistryEntity> {

	private static final long serialVersionUID = 1L;

	public SshPubKeyRegistrySendMailExecutor() {
		super();
	}

	@Override
	public void execute() {
		Logger logger = LoggerFactory.getLogger(SshPubKeyRegistrySendMailExecutor.class);
		logger.debug("Executing");
		
		String templateName = getJobStore().get("mail_template");

		if (templateName == null) {
			logger.warn("No template configured for SshPubKeyRegistrySendMailExecutor");
			return;
		}
		
		try {
			InitialContext ic = new InitialContext();
			
			TemplateMailService templateMailService = (TemplateMailService) ic.lookup("global/bwreg/bwreg-service/TemplateMailServiceImpl!edu.kit.scc.webreg.service.mail.TemplateMailService");
			
			SshPubKeyRegistryEntity sshPubKeyRegistry = getEvent().getEntity();
			Map<String, Object> context = new HashMap<String, Object>(3);
			context.put("sshPubKeyRegistry", sshPubKeyRegistry);
			context.put("sshPubKey", sshPubKeyRegistry.getSshPubKey());
			context.put("registry", sshPubKeyRegistry.getRegistry());
			context.put("service", sshPubKeyRegistry.getRegistry().getService());
			context.put("user", sshPubKeyRegistry.getSshPubKey().getUser());
			
			templateMailService.sendMail(templateName, context, true);
			
		} catch (NamingException e) {
			logger.warn("Could not send email: {}", e);
		}
		
	}

}
