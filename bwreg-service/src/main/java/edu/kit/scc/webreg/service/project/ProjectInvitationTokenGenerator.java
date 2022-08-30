package edu.kit.scc.webreg.service.project;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.project.ProjectInvitationTokenDao;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationType;

@ApplicationScoped
public class ProjectInvitationTokenGenerator implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private ProjectInvitationTokenDao dao;

	public ProjectInvitationTokenEntity sendToken(ProjectEntity project, String rcptMail, String rcptName, String senderName, String customMessage) {
		ProjectInvitationTokenEntity token = generateToken(project, rcptMail, rcptName, senderName, customMessage);
		
		/**
		 * TODO send token here
		 */
		
		return token;
	}
	
	public ProjectInvitationTokenEntity generateToken(ProjectEntity project, String rcptMail, String rcptName, String senderName, String customMessage) {
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
		
		Long validity = 14 * 24 * 60 * 60 * 1000L;
		if (appConfig.getConfigValue("invitation_token_validity") != null) {
			validity = Long.parseLong(appConfig.getConfigValue("invitation_token_validity"));
		}
		token.setValidUntil(new Date(System.currentTimeMillis() + validity));
		
		token = dao.persist(token);
		
		return token;
	}
	
}
