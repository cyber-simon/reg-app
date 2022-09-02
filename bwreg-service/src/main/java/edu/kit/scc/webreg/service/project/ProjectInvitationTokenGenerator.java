package edu.kit.scc.webreg.service.project;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.project.ProjectInvitationTokenDao;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationType;
import edu.kit.scc.webreg.service.mail.TemplateMailService;

@ApplicationScoped
public class ProjectInvitationTokenGenerator implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private ProjectInvitationTokenDao dao;

	@Inject
	private TemplateMailService mailService;
	
	public ProjectInvitationTokenEntity sendToken(ProjectEntity project, IdentityEntity identity, String rcptMail, String rcptName, String senderName, String customMessage) {
		ProjectInvitationTokenEntity token = generateToken(project, identity, rcptMail, rcptName, senderName, customMessage);
		sendToken(token);
		
		return token;
	}
	
	public void sendToken(ProjectInvitationTokenEntity token) {

		logger.debug("Sending ProjectInvitationToken mail for project {} (tokenId: {})", token.getProject().getName(), token.getId());

		String templateName = appConfig.getConfigValueOrDefault("project_invitation_template", "project_invitation");
		
		Map<String, Object> context = new HashMap<String, Object>(3);
		context.put("token", token);
		context.put("identity", token.getIdentity());
		context.put("project", token.getProject());
		
		mailService.sendMail(templateName, context, true);

	}
	
	public ProjectInvitationTokenEntity generateToken(ProjectEntity project, IdentityEntity identity, String rcptMail, String rcptName, String senderName, String customMessage) {

		logger.debug("Creating ProjectInvitationToken for project {} (rcptMail {}, rcptName {})", project.getName(), rcptMail, rcptName);
		
		ProjectInvitationTokenEntity token = dao.createNew();
		token.setType(ProjectInvitationType.ONE_TIME);

		SecureRandom random = new SecureRandom();
		String t = new BigInteger(130, random).toString(32);
		token.setToken(t);
		token.setRcptMail(rcptMail);
		token.setRcptName(rcptName);
		token.setSenderName(senderName);
		token.setCustomMessage(customMessage);
		token.setProject(project);
		token.setIdentity(identity);
		
		Long validity = 14 * 24 * 60 * 60 * 1000L;
		if (appConfig.getConfigValue("invitation_token_validity") != null) {
			validity = Long.parseLong(appConfig.getConfigValue("invitation_token_validity"));
		}
		token.setValidUntil(new Date(System.currentTimeMillis() + validity));
		
		token = dao.persist(token);
		
		return token;
	}
	
}
