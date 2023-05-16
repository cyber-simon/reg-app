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

import edu.kit.scc.regapp.mail.api.TemplateMailService;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.project.ProjectInvitationTokenDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationStatus;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationType;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.ProjectInvitationTokenEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;

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
	private LocalProjectUpdater updater;

	@Inject
	private TemplateMailService mailService;

	@Inject
	private EventSubmitter eventSubmitter;

	public void acceptEmailToken(ProjectInvitationTokenEntity token, IdentityEntity identity, String executor) {
		updater.addProjectMember(token.getProject(), identity, executor);
		token.setInvitedIdentity(identity);
		token.setStatus(ProjectInvitationStatus.ACCEPTED);
		token.setLastStatusChange(new Date());

		ProjectInvitationTokenEvent event = new ProjectInvitationTokenEvent(token);
		try {
			eventSubmitter.submit(event, EventType.PROJECT_INVITATION_EMAIL_ACCEPTED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}

	public void declineEmailToken(ProjectInvitationTokenEntity token, IdentityEntity identity, String executor) {
		ProjectInvitationTokenEvent event = new ProjectInvitationTokenEvent(token);
		token.setInvitedIdentity(identity);
		token.setStatus(ProjectInvitationStatus.DECLINED);
		token.setLastStatusChange(new Date());

		try {
			eventSubmitter.submit(event, EventType.PROJECT_INVITATION_EMAIL_DECLINED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}

	public ProjectInvitationTokenEntity sendToken(ProjectEntity project, IdentityEntity identity, String rcptMail,
			String rcptName, String senderName, String customMessage, String executor) {
		ProjectInvitationTokenEntity token = generateToken(project, identity, rcptMail, rcptName, senderName,
				customMessage, executor);
		sendToken(token);

		return token;
	}

	public void sendToken(ProjectInvitationTokenEntity token) {

		logger.debug("Sending ProjectInvitationToken mail for project {} (tokenId: {})", token.getProject().getName(),
				token.getId());

		String templateName = appConfig.getConfigValueOrDefault("project_invitation_template", "project_invitation");

		Map<String, Object> context = new HashMap<String, Object>(3);
		context.put("token", token);
		context.put("identity", token.getIdentity());
		context.put("project", token.getProject());

		mailService.sendMail(templateName, context, true);
		token.setStatus(ProjectInvitationStatus.MAIL_SENT);
		token.setLastStatusChange(new Date());

	}

	public ProjectInvitationTokenEntity generateToken(ProjectEntity project, IdentityEntity identity, String rcptMail,
			String rcptName, String senderName, String customMessage, String executor) {

		logger.debug("Creating ProjectInvitationToken for project {} (rcptMail {}, rcptName {})", project.getName(),
				rcptMail, rcptName);

		ProjectInvitationTokenEntity token = dao.createNew();
		token.setType(ProjectInvitationType.ONE_TIME);
		token.setStatus(ProjectInvitationStatus.NEW);
		token.setLastStatusChange(new Date());

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

		ProjectInvitationTokenEvent event = new ProjectInvitationTokenEvent(token);
		try {
			eventSubmitter.submit(event, EventType.PROJECT_INVITATION_EMAIL_CREATED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		return token;
	}

}
