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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthRpConfigurationEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.oauth.OAuthRpConfigurationService;
import edu.kit.scc.webreg.service.oauth.client.OAuthUserCreateService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class ConnectAccountOAuthBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserService userService;

	@Inject
	private IdentityService identityService;

	@Inject
	private SessionManager sessionManager;

	@Inject
	private FacesMessageGenerator messageGenerator;

    @Inject
    private OAuthRpConfigurationService rpConfigService;

	@Inject
	private OAuthUserCreateService userCreateService;

	private IdentityEntity identity;
	private List<UserEntity> userList;

	private OAuthRpConfigurationEntity rpConfig;

	private String pin;

	private Boolean errorState = false;

	private Map<String, String> printableAttributesMap;
	private Map<String, String> unprintableAttributesMap;
	private List<String> printableAttributesList;

	private OAuthUserEntity entity;

	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			identity = identityService.fetch(sessionManager.getIdentityId());
		}

		if (sessionManager.getOauthRelyingPartyId() == null) {
			errorState = true;
			messageGenerator.addResolvedErrorMessage("page-not-directly-accessible",
					"page-not-directly-accessible-text", true);
			return;
		}

		rpConfig = rpConfigService.fetch(sessionManager.getOauthRelyingPartyId());

		if (rpConfig == null) {
			errorState = true;
			messageGenerator.addResolvedErrorMessage("page-not-directly-accessible",
					"page-not-directly-accessible-text", true);
			return;
		}

		printableAttributesMap = new HashMap<String, String>();
		unprintableAttributesMap = new HashMap<String, String>();
		printableAttributesList = new ArrayList<String>();

		try {
			entity = userCreateService.preCreateUser(rpConfig.getId(), sessionManager.getLocale(),
					sessionManager.getAttributeMap());

		} catch (UserUpdateException e) {
			errorState = true;
			messageGenerator.addResolvedErrorMessage("missing-mandatory-attributes", e.getMessage(), true);
			return;
		}

		printableAttributesList.add("subject_id");
		printableAttributesMap.put("subject_id", entity.getOauthId());
		printableAttributesList.add("issuer");
		printableAttributesMap.put("issuer", rpConfig.getServiceUrl());
		printableAttributesList.add("name");
		printableAttributesMap.put("name", entity.getName());

		@SuppressWarnings("unchecked")
		HashMap<String, Object> userMap = (HashMap<String, Object>) sessionManager.getAttributeMap().get("user").get(0);
		for (Entry<String, Object> entry : userMap.entrySet()) {
			if (entry.getValue() != null)
				unprintableAttributesMap.put(entry.getKey(), entry.getValue().toString());
		}
	}

	public String save() {
		try {
			entity = userCreateService.createAndLinkUser(identity, entity, sessionManager.getAttributeMap(), null);
			entity = userCreateService.postCreateUser(entity, sessionManager.getAttributeMap(), "user-" + entity.getId());
		} catch (UserUpdateException e) {
			logger.warn("An error occured whilst creating user", e);
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			return null;
		}

		return "/user/index.xhtml";
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null)
			userList = userService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(UserEntity_.id)),
					equal(UserEntity_.identity, getIdentity()), UserEntity_.genericStore, UserEntity_.attributeStore,
					SamlUserEntity_.idp);

		return userList;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public Map<String, String> getPrintableAttributesMap() {
		return printableAttributesMap;
	}

	public List<String> getPrintableAttributesList() {
		return printableAttributesList;
	}

	public OAuthUserEntity getEntity() {
		return entity;
	}

	public Map<String, String> getUnprintableAttributesMap() {
		return unprintableAttributesMap;
	}

	public Boolean getErrorState() {
		return errorState;
	}

}
