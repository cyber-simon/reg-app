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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.twofa.LinotpTokenResultList;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class TwoFaUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserService userService;

	@Inject
	private TwoFaService twoFaService;
	
    @Inject 
    private SessionManager sessionManager;
    
	@Inject
	private FacesMessageGenerator messageGenerator;
	    
	private UserEntity user;
	private LinotpTokenResultList tokenList;
	private LinotpInitAuthenticatorTokenResponse createTokenResponse;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
	    	user = userService.findById(sessionManager.getUserId());
	    	try {
				tokenList = twoFaService.findByUserId(sessionManager.getUserId());
			} catch (TwoFaException e) {
				messageGenerator.addErrorMessage("Error", e.toString());
				logger.debug("Exception happened", e);
			}
		}
	}
	
	public void createAuthenticatorToken() {
		try {
			createTokenResponse = twoFaService.createAuthenticatorToken(user.getId());
		} catch (TwoFaException e) {
			logger.warn("TwoFaException", e);
		}
	}
	
	public Boolean getReadOnly() {
		return tokenList.getReadOnly();
	}
	
	public String getManagementUrl() {
		return tokenList.getManagementUrl();
	}
	
	public LinotpTokenResultList getTokenList() {
		return tokenList;
	}

	public UserEntity getUser() {
		return user;
	}

	public LinotpInitAuthenticatorTokenResponse getCreateTokenResponse() {
		return createTokenResponse;
	}

}
