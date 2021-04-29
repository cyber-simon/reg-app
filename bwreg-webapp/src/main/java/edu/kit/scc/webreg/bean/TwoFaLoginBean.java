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
import java.time.Instant;

import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.UserLoginInfoStatus;
import edu.kit.scc.webreg.entity.UserLoginMethod;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpTokenResultList;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class TwoFaLoginBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	@Inject
	private IdentityService identityService;

	@Inject
	private UserService userService;

	@Inject
	private TwoFaService twoFaService;

	private IdentityEntity identity;
	private LinotpTokenResultList tokenList;
	
	private String tokenInput;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			identity = identityService.findById(sessionManager.getIdentityId());
			try {
				tokenList = twoFaService.findByIdentity(identity);
			} catch (TwoFaException e) {
				messageGenerator.addErrorMessage("Error", e.toString());
				logger.debug("Exception happened", e);
			}
		}
	}
	
	public void check() {
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			LinotpSimpleResponse response = twoFaService.checkToken(identity, tokenInput);

			if (response.getResult() != null && response.getResult().isStatus() && response.getResult().isValue()) {
				// Successful check
				sessionManager.setTwoFaElevation(Instant.now());
				userService.addLoginInfo(identity, UserLoginMethod.TWOFA, UserLoginInfoStatus.SUCCESS, 
						request.getRemoteAddr());
				
	    		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
				context.redirect(sessionManager.getOriginalRequestPath());
				sessionManager.setOriginalRequestPath(null);
			}
			else {
				userService.addLoginInfo(identity, UserLoginMethod.TWOFA, UserLoginInfoStatus.FAILED, 
						request.getRemoteAddr());
				messageGenerator.addResolvedWarningMessage("twofa_login_failed", "twofa_login_failed_detail", true);
				tokenInput = "";
			}
		} catch (TwoFaException e) {
			messageGenerator.addErrorMessage("Error", e.toString());
			logger.debug("Exception happened", e);
		} catch (IOException e) {
			logger.warn("Could not redirect client", e);
			throw new IllegalArgumentException("redirect failed");
		}
		
	}

	public Boolean getReadOnly() {
		return tokenList.getReadOnly();
	}
	
	public Boolean getReallyReadOnly() {
		return tokenList.getReallyReadOnly();
	}
	
	public String getManagementUrl() {
		return tokenList.getManagementUrl();
	}	

	public LinotpTokenResultList getTokenList() {
		return tokenList;
	}

	public String getTokenInput() {
		return tokenInput;
	}

	public void setTokenInput(String tokenInput) {
		this.tokenInput = tokenInput;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}
	
}
