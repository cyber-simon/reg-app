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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.PolicyEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectInvitationTokenEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.entity.project.ProjectPolicyType;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import edu.kit.scc.webreg.service.PolicyService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.project.ProjectInvitationTokenService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import edu.kit.scc.webreg.util.PolicyHolder;

@Named
@ViewScoped
public class AcceptInviteBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(AcceptInviteBean.class);

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

	@Inject
	private PolicyService policyService;

	private IdentityEntity identity;
	private String tokenString;
	private ProjectInvitationTokenEntity token;
	private ProjectEntity project;
	private Map<ProjectServiceEntity, List<PolicyEntity>> servicePolicyMap;
	private List<ProjectServiceEntity> projectServiceList;

	private List<PolicyHolder> policyHolderList;

	public void preRenderView(ComponentSystemEvent ev) {

	}

	public void check() {
		token = tokenService.findByAttr("token", tokenString);
		if (token == null) {
			messageGenerator.addResolvedErrorMessage("no-token", "no-token-detail", true);
			return;
		}
		else if (token.getValidUntil().before(new Date())) {
			messageGenerator.addResolvedErrorMessage("project.accept_invite.token_expired", "project.accept_invite.token_expired_detail", true);
			token = null;
			return;
		}

		project = token.getProject();

		ProjectMembershipEntity pme = projectService.findByIdentityAndProject(getIdentity(), project);
		if (pme != null) {
			messageGenerator.addResolvedErrorMessage("project.accept_invite.already_project_member",
					"project.accept_invite.already_project_member_detail", true);
		}

		servicePolicyMap = policyService.findPolicyMapForProject(project, ProjectPolicyType.MEMBER_ACCEPT);
		projectServiceList = servicePolicyMap.keySet().stream().collect(Collectors.toList());
		policyHolderList = new ArrayList<PolicyHolder>();

		projectServiceList.stream().forEach(projectServiceEntity -> {
			servicePolicyMap.get(projectServiceEntity).stream().forEach(policy -> {
				PolicyHolder ph = new PolicyHolder();
				ph.setPolicy(policy);
				ph.setChecked(false);
				policyHolderList.add(ph);
			});
		});
	}

	public void accept() {
		if (token == null) {
			messageGenerator.addResolvedErrorMessage("no-token", "no-token-detail", true);
			return;
		}
		else if (token.getValidUntil().before(new Date())) {
			messageGenerator.addResolvedErrorMessage("project.accept_invite.token_expired", "project.accept_invite.token_expired_detail", true);
			token = null;
			return;
		}
		
		logger.debug("testing all checkboxes");
		for (PolicyHolder ph : policyHolderList) {
			if (ph.getPolicy() != null && ph.getPolicy().getShowOnly() != null && ph.getPolicy().getShowOnly()) {
				logger.debug("Policy {} in Service {} is just for show", ph.getPolicy().getId(), ph.getPolicy().getProjectPolicy().getId());
			} else if (!ph.getChecked()) {
				logger.debug("Policy {} in Service {} is not checked", ph.getPolicy().getId(), ph.getPolicy().getProjectPolicy().getId());
				messageGenerator.addWarningMessage("need_check", "Zustimmung fehlt!",
						"Sie müssen allen Nutzungbedingungen zustimmen.");
				return;
			} else {
				logger.debug("Policy {} in Service {} is checked", ph.getPolicy().getId(), ph.getPolicy().getProjectPolicy().getId());
			}
		}

		tokenService.acceptEmailToken(token, sessionManager.getIdentityId(),
				"idty-" + sessionManager.getIdentityId());
		token = null;
		tokenString = "";
	}

	public void decline() {
		tokenService.declineEmailToken(token, sessionManager.getIdentityId(), "idty-" + sessionManager.getIdentityId());
		token = null;
		tokenString = "";
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.fetch(sessionManager.getIdentityId());
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

	public Map<ProjectServiceEntity, List<PolicyEntity>> getServicePolicyMap() {
		return servicePolicyMap;
	}

	public List<ProjectServiceEntity> getProjectServiceList() {
		return projectServiceList;
	}

	public List<PolicyHolder> getPolicyHolderList() {
		return policyHolderList;
	}
}
