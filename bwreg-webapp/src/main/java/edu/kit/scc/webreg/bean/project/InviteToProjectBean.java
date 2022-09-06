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
package edu.kit.scc.webreg.bean.project;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.LocalProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectAdminType;
import edu.kit.scc.webreg.entity.project.ProjectIdentityAdminEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.project.LocalProjectService;
import edu.kit.scc.webreg.service.project.ProjectInvitationTokenService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class InviteToProjectBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private SessionManager session;
	
	@Inject
	private LocalProjectService service;

	@Inject
	private ProjectService projectService;

	@Inject
	private ProjectInvitationTokenService tokenService;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	@Inject
	private IdentityService identityService;
	
	@Inject
	private UserService userService;
	
	private LocalProjectEntity entity;
	private List<ProjectInvitationTokenEntity> tokenList;
	
	private Long id;

	private List<ProjectIdentityAdminEntity> adminList;
	private ProjectIdentityAdminEntity adminIdentity;
	private IdentityEntity identity;
	
	@Email
	@NotNull
	private String rcptMail;
	
	private String rcptName;
	private String senderName;
	private String senderMail;
	private String customMessage;
	
	private Set<String> senderEmailList;
	private Set<String> senderNameList;
	
	public void preRenderView(ComponentSystemEvent ev) {
		
		for (ProjectIdentityAdminEntity a : getAdminList()) {
			if (a.getIdentity().getId().equals(getIdentity().getId())) {
				adminIdentity = a;
				break;
			}
		}
		
		if (adminIdentity == null) {
			throw new NotAuthorizedException("Nicht autorisiert");
		}		
		else {
			if (! (ProjectAdminType.ADMIN.equals(adminIdentity.getType()) || ProjectAdminType.OWNER.equals(adminIdentity.getType()))) {
				throw new NotAuthorizedException("Nicht autorisiert");
			}
		}
	}

	public void sendToken() {
		tokenService.sendEmailToken(getEntity(), getIdentity(), rcptMail, rcptName, senderName, customMessage, "idty-" + session.getIdentityId());
		messageGenerator.addResolvedInfoMessage("project.invite_project.token_send", "project.invite_project.token_send_detail", true);
		tokenList = null;
	}
	
	public void deleteToken(ProjectInvitationTokenEntity token) {
		tokenService.delete(token);
		messageGenerator.addResolvedInfoMessage("project.invite_project.token_deleted", "project.invite_project.token_deleted_detail", true);
		tokenList = null;
	}
	
	public void resendToken(ProjectInvitationTokenEntity token) {
		
	}
	
	public List<ProjectIdentityAdminEntity> getAdminList() {
		if (adminList == null) {
			adminList = projectService.findAdminsForProject(getEntity());
		}
		return adminList;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalProjectEntity getEntity() {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id);
		}

		return entity;
	}

	public void setEntity(LocalProjectEntity entity) {
		this.entity = entity;
	}

	public List<ProjectInvitationTokenEntity> getTokenList() {
		if (tokenList == null) {
			tokenList = tokenService.findAllByAttr("project", getEntity());
		}
		
		return tokenList;
	}

	public String getRcptMail() {
		return rcptMail;
	}

	public void setRcptMail(String rcptMail) {
		this.rcptMail = rcptMail;
	}

	public String getRcptName() {
		return rcptName;
	}

	public void setRcptName(String rcptName) {
		this.rcptName = rcptName;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getCustomMessage() {
		return customMessage;
	}

	public void setCustomMessage(String customMessage) {
		this.customMessage = customMessage;
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.findById(session.getIdentityId());
		}
		return identity;
	}

	public Set<String> getSenderEmailList() {
		if (senderEmailList == null) {
			fillSenderLists();
		}
		return senderEmailList;
	}

	public Set<String> getSenderNameList() {
		if (senderNameList == null) {
			fillSenderLists();
		}
		return senderNameList;
	}
	
	private void fillSenderLists() {
		List<UserEntity> userList = userService.findByIdentity(getIdentity());
		senderEmailList = new HashSet<String>();
		senderNameList = new HashSet<String>();
		
		for (UserEntity user : userList) {
			if (user.getEmail() != null) {
				senderEmailList.add(user.getEmail());
			}
			
			if (user.getEmailAddresses() != null) {
				senderEmailList.addAll(user.getEmailAddresses());
			}
			
			if (user.getGivenName() != null && user.getSurName() != null) {
				senderNameList.add(user.getGivenName() + " " + user.getSurName());
			}
		}
	}

	public String getSenderMail() {
		return senderMail;
	}

	public void setSenderMail(String senderMail) {
		this.senderMail = senderMail;
	}
}
