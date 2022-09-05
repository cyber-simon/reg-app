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
package edu.kit.scc.webreg.bean;

import java.io.Serializable;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.project.ProjectInvitationTokenService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class AcceptInviteBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
    private IdentityService identityService;
    
    @Inject 
    private SessionManager sessionManager;

    @Inject
    private ProjectInvitationTokenService tokenService;
    
    @Inject
    private FacesMessageGenerator messageGenerator;
    
    @Inject
    private ProjectService projectService;
    
	private IdentityEntity identity;
	private String tokenString;
	private ProjectInvitationTokenEntity token;
	
	public void preRenderView(ComponentSystemEvent ev) {
		
	}

	public void check() {
		token = tokenService.findByAttr("token", tokenString);
		if (token == null) {
			messageGenerator.addResolvedErrorMessage("no-token", "no-token-detail", true);
			return;
		}
		
		ProjectMembershipEntity pme = projectService.findByIdentityAndProject(getIdentity(), token.getProject());
		if (pme != null) {
			messageGenerator.addResolvedErrorMessage("already-project-member", "already-project-member-detail", true);
		}
	}

	public void accept() {
		if (token != null) {
			projectService.addProjectMember(token.getProject(), token.getIdentity(), "idty-" + sessionManager.getIdentityId());
			tokenService.delete(token);
			token = null;
		}
		else {
			messageGenerator.addResolvedErrorMessage("no-token", "no-token-detail", true);
		}
	}
	
	public void decline() {
		token = null;
	}
	
	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.findById(sessionManager.getIdentityId());
		}
		return identity;
	}
	
	public String getTokenString() {
		if (tokenString != null) {
			tokenString = tokenString.replaceAll("[^a-zA-Z0-9-_]", "");
		}
		return tokenString;
	}

	public void setTokenString(String tokenString) {
		this.tokenString = tokenString;
	}

	public ProjectInvitationTokenEntity getToken() {
		return token;
	}
}
