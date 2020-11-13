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

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.EmailTemplateDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.EmailTemplateEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.MailServiceException;
import edu.kit.scc.webreg.exc.TemplateRenderingException;
import edu.kit.scc.webreg.service.identity.IdentityUserPrefsResolver;
import edu.kit.scc.webreg.service.mail.MailService;
import edu.kit.scc.webreg.service.mail.QueuedMailService;
import edu.kit.scc.webreg.service.mail.TemplateMailService;
import edu.kit.scc.webreg.service.mail.TemplateRenderer;

@Stateless
public class TemplateMailServiceImpl implements TemplateMailService {

	@Inject
	private Logger logger;
	
	@Inject
	private TemplateRenderer renderer;
	
	@Inject
	private QueuedMailService queuedMailService;
	
	@Inject
	private MailService mailService;
	
	@Inject
	private EmailTemplateDao emailTemplateDao;
	
	@Inject
	private IdentityUserPrefsResolver userPrefsResolver;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private IdentityDao identityDao;
	
	@Override
	public void sendMail(String templateName, Map<String, Object> rendererContext, Boolean queued) {
		
		try {
			logger.debug("Looking up template {} in database", templateName);
			
			EmailTemplateEntity emailTemplateEntity = emailTemplateDao.findByName(templateName);
			
			if (emailTemplateEntity == null) {
				logger.warn("No template found by name {}", templateName);
				return;
			}

			if (rendererContext.containsKey("identity")) {
				IdentityEntity identity = identityDao.merge((IdentityEntity) rendererContext.get("identity"));
				rendererContext.putAll(userPrefsResolver.resolvePrefs(identity));
				if ((! rendererContext.containsKey("user")) && (identity.getPrefUser() != null)) {
					rendererContext.put("user", identity.getPrefUser());
				}
			}
			else if (rendererContext.containsKey("user")) {
				UserEntity user = userDao.merge((UserEntity) rendererContext.get("user"));
				rendererContext.putAll(userPrefsResolver.resolvePrefs(user.getIdentity()));
				rendererContext.put("identity", user.getIdentity());
			}
			
			logger.debug("Rendering Email");
			
			String body = renderer.evaluate(emailTemplateEntity.getBody(), rendererContext);
			String to = renderer.evaluate(emailTemplateEntity.getTo(), rendererContext);
			String cc = renderer.evaluate(emailTemplateEntity.getCc(), rendererContext);
			String bcc = renderer.evaluate(emailTemplateEntity.getBcc(), rendererContext);
			String from = renderer.evaluate(emailTemplateEntity.getFrom(), rendererContext);
			String subject = renderer.evaluate(emailTemplateEntity.getSubject(), rendererContext);

			if (queued)
				queuedMailService.sendMail(from, to, cc, bcc, subject, body);
			else
				mailService.sendMail(from, to, cc, bcc, subject, body);

		} catch (TemplateRenderingException e) {
			logger.warn("Problem while processing template", e);
		} catch (MailServiceException e) {
			logger.warn("Problem while sending template", e);
		}
	}
}
