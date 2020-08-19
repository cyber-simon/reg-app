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
import java.time.Instant;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpGetBackupTanListResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpInitAuthenticatorTokenResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
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
	
	private UserEntity user;
	private LinotpTokenResultList tokenList;
	private LinotpInitAuthenticatorTokenResponse createTokenResponse;
	
	private String totpCode, yubicoCode;
	private String defaultButton;
	
	private LinotpGetBackupTanListResponse backupTanList;
	
	private Long returnServiceId;
	
	public void preRenderView(ComponentSystemEvent ev) {

		defaultButton = "yubicoStartButton";
		
		if (user == null) {
	    	try {
				tokenList = twoFaService.findByUserId(sessionManager.getUserId());
			} catch (TwoFaException e) {
				messageGenerator.addErrorMessage("Error", e.toString());
				logger.debug("Exception happened", e);
			}

	    	user = userService.findById(sessionManager.getUserId());
		}
	}
	
	public void createAuthenticatorToken() {
		if (! getReadOnly()) {
			try {
				createTokenResponse = twoFaService.createAuthenticatorToken(user.getId(), "user-" + user.getId());
				tokenList = twoFaService.findByUserId(sessionManager.getUserId());
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
			}
		}
	}

	public void createYubicoToken() {
		if (! getReadOnly()) {
			try {
				LinotpInitAuthenticatorTokenResponse response = twoFaService.createYubicoToken(user.getId(), yubicoCode, "user-" + user.getId());

				if (response.getResult().isStatus() && response.getResult().isValue()) {
					if (response != null && response.getDetail() != null) {
						String serial = response.getDetail().getSerial();
						twoFaService.initToken(user.getId(), serial, "user-" + user.getId());
						
					}
					
					tokenList = twoFaService.findByUserId(sessionManager.getUserId());
					if (tokenList.size() == 1) {
						// this was the first token. We have to set 2fa elevation
						sessionManager.setTwoFaElevation(Instant.now());
					}					
				}
				else {
					messageGenerator.addResolvedWarningMessage("warn", "twofa_token_failed", true);
				}
	
				PrimeFaces.current().executeScript("PF('addYubicoDlg').hide();");
				createTokenResponse = null;
				yubicoCode = "";
				
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
			}
		}
	}

	public void createBackupTanList() {
		if (! getReadOnly()) {
			try {
				LinotpInitAuthenticatorTokenResponse response = twoFaService.createBackupTanList(user.getId(), "user-" + user.getId());

				if (response.getResult().isStatus() && response.getResult().isValue()) {
					if (response != null && response.getDetail() != null) {
						String serial = response.getDetail().getSerial();
						twoFaService.initToken(user.getId(), serial, "user-" + user.getId());
						
					}
					
					tokenList = twoFaService.findByUserId(sessionManager.getUserId());
					if (tokenList.size() == 1) {
						// this was the first token. We have to set 2fa elevation
						sessionManager.setTwoFaElevation(Instant.now());
					}					
				}
				else {
					messageGenerator.addResolvedWarningMessage("warn", "twofa_token_failed", true);
				}
	
				PrimeFaces.current().executeScript("PF('addBackupTanDlg').hide();");
				createTokenResponse = null;
				yubicoCode = "";
				
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
			}
		}
	}	

	public void getBackupTanList(String serial) {
		if (! getReadOnly()) {
			try {
				backupTanList = twoFaService.getBackupTanList(user.getId(), serial, "user-" + user.getId());
				
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
			}
		}
	}
	
	public void checkAuthenticatorToken() {
		try {
			if (createTokenResponse != null && createTokenResponse.getDetail() != null) {
				String serial = createTokenResponse.getDetail().getSerial();
				LinotpSimpleResponse response = twoFaService.enableToken(user.getId(), serial, "user-" + user.getId());

				if (response.getResult() != null && response.getResult().isStatus() && response.getResult().isValue()) {
				
					response = twoFaService.checkSpecificToken(user.getId(), serial, totpCode);
					if (response.getResult() != null && response.getResult().isStatus() && response.getResult().isValue()) {
						// success, Token stays active, set correct description
						twoFaService.initToken(user.getId(), serial, "user-" + user.getId());
						tokenList = twoFaService.findByUserId(sessionManager.getUserId());
						if (tokenList.size() == 1) {
							// this was the first token. We have to set 2fa elevation
							sessionManager.setTwoFaElevation(Instant.now());
						}
						PrimeFaces.current().executeScript("PF('addTotpDlg').hide();");
						createTokenResponse = null;
						totpCode = "";
					} 
					else {
						// wrong code, disable token
						response = twoFaService.disableToken(user.getId(), serial, "user-" + user.getId());
						totpCode = "";
					}
				}
			}
		} catch (TwoFaException e) {
			logger.warn("TwoFaException", e);
		}
	}

	public void enableToken(String serial) {
		if (! getReadOnly()) {
			try {
				LinotpSimpleResponse response = twoFaService.enableToken(user.getId(), serial, "user-" + user.getId());
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
	}

	public void disableToken(String serial) {
		if (! getReadOnly()) {
			try {
				LinotpSimpleResponse response = twoFaService.disableToken(user.getId(), serial, "user-" + user.getId());
				tokenList = twoFaService.findByUserId(sessionManager.getUserId());
				if ((response.getResult() != null) && response.getResult().isStatus() &&
						response.getResult().isValue()) {
					messageGenerator.addInfoMessage("Info", "Token " + serial + " disabled");
				}
				else {
					messageGenerator.addWarningMessage("Warn", "Token " + serial + " could not be disable");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("Error", e.toString());
			}
		}
	}

	public void deleteToken(String serial) {
		if (! getReadOnly()) {
			try {
				LinotpSimpleResponse response = twoFaService.deleteToken(user.getId(), serial, "user-" + user.getId());
				tokenList = twoFaService.findByUserId(sessionManager.getUserId());
				if ((response.getResult() != null) && response.getResult().isStatus() &&
						response.getResult().isValue()) {
					messageGenerator.addInfoMessage("Info", "Token " + serial + " deleted");
				}
				else {
					messageGenerator.addWarningMessage("Warn", "Token " + serial + " could not be deleted");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("Error", e.toString());
			}
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

	public String getTotpCode() {
		return totpCode;
	}

	public void setTotpCode(String totpCode) {
		this.totpCode = totpCode;
	}

	public String getDefaultButton() {
		return defaultButton;
	}

	public void setDefaultButton(String defaultButton) {
		this.defaultButton = defaultButton;
	}

	public String getYubicoCode() {
		return yubicoCode;
	}

	public void setYubicoCode(String yubicoCode) {
		this.yubicoCode = yubicoCode;
	}

	public Long getReturnServiceId() {
		return returnServiceId;
	}

	public void setReturnServiceId(Long returnServiceId) {
		// make this not overwriteable. Ajax requests would overwrite this parameter
		if (returnServiceId != null) {
			this.returnServiceId = returnServiceId;
		}
	}

	public LinotpGetBackupTanListResponse getBackupTanList() {
		return backupTanList;
	}

}
