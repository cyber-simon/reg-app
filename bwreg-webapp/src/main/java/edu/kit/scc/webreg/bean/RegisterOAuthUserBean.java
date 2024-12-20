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

import java.io.IOException;
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
import edu.kit.scc.webreg.service.oauth.OAuthRpConfigurationService;
import edu.kit.scc.webreg.service.oauth.client.OAuthUserCreateService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ViewScoped
public class RegisterOAuthUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject 
    private SessionManager sessionManager;
	
    @Inject
    private UserService service;

    @Inject
    private OAuthUserCreateService userCreateService;

    @Inject
    private OAuthRpConfigurationService rpConfigService;
    
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private OAuthUserEntity entity;
	private List<UserEntity> oldUserList;
	private OAuthRpConfigurationEntity rpConfig;
	
	private Boolean errorState = false;
	private Boolean eppnError = false;
	private Boolean eppnOverride = false;

	private Map<String, String> printableAttributesMap;
	private Map<String, String> unprintableAttributesMap;
	private List<String> printableAttributesList;
	
	private IdentityEntity identity;
	private List<UserEntity> userList;
	
    public void preRenderView(ComponentSystemEvent ev) {
    	if (entity == null) {
    		
	    	if (sessionManager.getOauthRelyingPartyId() == null) {
				errorState = true;
				messageGenerator.addResolvedErrorMessage("page-not-directly-accessible", "page-not-directly-accessible-text", true);
				return;    		
	    	}
	    	
	    	rpConfig = rpConfigService.fetch(sessionManager.getOauthRelyingPartyId());
	
	    	if (rpConfig == null) {
				errorState = true;
				messageGenerator.addResolvedErrorMessage("page-not-directly-accessible", "page-not-directly-accessible-text", true);
				return;    		
	    	}
	
	    	printableAttributesMap = new HashMap<String, String>();
	    	unprintableAttributesMap = new HashMap<String, String>();
	    	printableAttributesList = new ArrayList<String>();
	
	    	try {
	        	entity = userCreateService.preCreateUser(rpConfig.getId(),
	        			sessionManager.getLocale(), sessionManager.getAttributeMap());
	        	
	        	identity = userCreateService.preMatchIdentity(entity, sessionManager.getAttributeMap());
			} catch (UserUpdateException e) {
				errorState = true;
				messageGenerator.addResolvedErrorMessage("missing-mandatory-attributes", e.getMessage(), true);
				return;
			}

	    	oldUserList = service.findByEppn(entity.getEppn());
	    	if (oldUserList.size() > 0) {
				eppnError = true;
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
	}

    public String save() {

		if (errorState) {
			/*
			 * There are unresolved errors. Cannot persist user.
			 */
			return null;
		}
		else if (eppnError && (! eppnOverride)) {
			/*
			 * EPPN is already in system, but not aknowledged
			 */
			return null;
		}

		try {
			if (getIdentity() == null) {
				entity = userCreateService.createUser(entity, sessionManager.getAttributeMap(), null);
			}
			else {
				entity = userCreateService.createAndLinkUser(getIdentity(), entity, sessionManager.getAttributeMap(), null);
			}
			entity = userCreateService.postCreateUser(entity, sessionManager.getAttributeMap(), "user-" + entity.getId());
		} catch (UserUpdateException e) {
			logger.warn("An error occured whilst creating user", e);
			messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			return null;
		}

    	sessionManager.setIdentityId(entity.getIdentity().getId());
    	
		if (sessionManager.getOriginalRequestPath() != null) {
			String orig = sessionManager.getOriginalRequestPath();
			sessionManager.setOriginalRequestPath(null);
			
			ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
			try {
				externalContext.redirect(orig);
			} catch (IOException e) {
				messageGenerator.addResolvedErrorMessage("error_msg", e.toString(), false);
			}
			return null;
		}
		else
			return "/index.xhtml?faces-redirect=true";
    }
    
	public Boolean getErrorState() {
		return errorState;
	}

	public void setErrorState(Boolean errorState) {
		this.errorState = errorState;
	}

	public Map<String, String> getPrintableAttributesMap() {
		return printableAttributesMap;
	}

	public Map<String, String> getUnprintableAttributesMap() {
		return unprintableAttributesMap;
	}

	public List<String> getPrintableAttributesList() {
		return printableAttributesList;
	}

	public OAuthUserEntity getEntity() {
		return entity;
	}

	public void setEntity(OAuthUserEntity entity) {
		this.entity = entity;
	}

	public OAuthRpConfigurationEntity getRpConfig() {
		return rpConfig;
	}

	public Boolean getEppnOverride() {
		return eppnOverride;
	}

	public void setEppnOverride(Boolean eppnOverride) {
		this.eppnOverride = eppnOverride;
	}

	public Boolean getEppnError() {
		return eppnError;
	}

	public void setEppnError(Boolean eppnError) {
		this.eppnError = eppnError;
	}

	public List<UserEntity> getOldUserList() {
		return oldUserList;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null && getIdentity() != null)
			userList = service.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(UserEntity_.id)),
					equal(UserEntity_.identity, getIdentity()), UserEntity_.genericStore, UserEntity_.attributeStore,
					SamlUserEntity_.idp);

		return userList;
	}
}

