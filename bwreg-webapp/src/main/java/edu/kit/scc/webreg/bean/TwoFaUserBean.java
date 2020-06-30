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

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpToken;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpTokenResultList;
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
	
	@Inject
	private ApplicationConfig appConfig;
	    
	private UserEntity user;
	private LinotpTokenResultList tokenList;
	private LinotpInitAuthenticatorTokenResponse createTokenResponse;
	
	public void preRenderView(ComponentSystemEvent ev) {
		long elevationTime = 5L * 60L * 1000L;
		if (appConfig.getConfigValue("elevation_time") != null) {
			elevationTime = Long.parseLong(appConfig.getConfigValue("elevation_time"));
		}
		
		if (user == null) {
	    	try {
				tokenList = twoFaService.findByUserId(sessionManager.getUserId());
				
				for (LinotpToken token : tokenList) {
					if (token.getIsactive() && (sessionManager.getTwoFaElevation() == null ||
							(System.currentTimeMillis() - sessionManager.getTwoFaElevation().getTime()) > elevationTime)) {
			    		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
						context.redirect("/user/twofa-login.xhtml");
						sessionManager.setOriginalRequestPath("/user/twofa.xhtml");
					}
				}
			} catch (TwoFaException e) {
				messageGenerator.addErrorMessage("Error", e.toString());
				logger.debug("Exception happened", e);
			} catch (IOException e) {
				logger.warn("Could not redirect client", e);
				throw new IllegalArgumentException("redirect failed");
			}

	    	user = userService.findById(sessionManager.getUserId());
		}
	}
	
	public void createAuthenticatorToken() {
		try {
			createTokenResponse = twoFaService.createAuthenticatorToken(user.getId());
			tokenList = twoFaService.findByUserId(sessionManager.getUserId());
		} catch (TwoFaException e) {
			logger.warn("TwoFaException", e);
		}
	}
	
	public void enableToken(String serial) {
		try {
			LinotpSimpleResponse response = twoFaService.enableToken(user.getId(), serial);
			tokenList = twoFaService.findByUserId(sessionManager.getUserId());
			if ((response.getResult() != null) && response.getResult().isStatus() &&
					response.getResult().isValue()) {
				messageGenerator.addInfoMessage("Info", "Token " + serial + " enabled");
			}
			else {
				messageGenerator.addWarningMessage("Warn", "Token " + serial + " could not be enabled");
			}
		} catch (TwoFaException e) {
			logger.warn("TwoFaException", e);
			messageGenerator.addErrorMessage("Error", e.toString());
		}		
	}

	public void disableToken(String serial) {
		try {
			LinotpSimpleResponse response = twoFaService.disableToken(user.getId(), serial);
			tokenList = twoFaService.findByUserId(sessionManager.getUserId());
			if ((response.getResult() != null) && response.getResult().isStatus() &&
					response.getResult().isValue()) {
				messageGenerator.addInfoMessage("Info", "Token " + serial + " disable");
			}
			else {
				messageGenerator.addWarningMessage("Warn", "Token " + serial + " could not be disable");
			}
		} catch (TwoFaException e) {
			logger.warn("TwoFaException", e);
			messageGenerator.addErrorMessage("Error", e.toString());
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
