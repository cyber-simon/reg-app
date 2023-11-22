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
package edu.kit.scc.webreg.service.mail.impl;

import java.util.Map;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.regapp.mail.api.TemplateMailService;
import edu.kit.scc.regapp.mail.impl.TemplateMailSender;

@Stateless
public class TemplateMailServiceImpl implements TemplateMailService {

	private static final long serialVersionUID = 1L;

	@Inject
	private TemplateMailSender sender;
	
	@Override
	public void sendMail(String templateName, Map<String, Object> rendererContext, Boolean queued) {
		sender.sendMail(templateName, rendererContext, queued);
	}
}
