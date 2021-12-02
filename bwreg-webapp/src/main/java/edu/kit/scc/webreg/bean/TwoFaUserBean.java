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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpGetBackupTanListResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.token.GenericTwoFaToken;
import edu.kit.scc.webreg.service.twofa.token.TokenStatusResponse;
import edu.kit.scc.webreg.service.twofa.token.TotpCreateResponse;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class TwoFaUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private IdentityService identityService;

	@Inject
	private TwoFaService twoFaService;
	
    @Inject 
    private SessionManager sessionManager;
    
	@Inject
	private FacesMessageGenerator messageGenerator;
	
	private IdentityEntity identity;
	
	private TwoFaTokenList tokenList;
	private TotpCreateResponse createTokenResponse;
	
	private String totpCode, yubicoCode;
	private String defaultButton;
	
	private LinotpGetBackupTanListResponse backupTanList;
	
	private Long returnServiceId;
	
	public void preRenderView(ComponentSystemEvent ev) {

		defaultButton = "yubicoStartButton";
		
		if (identity == null) {
	    	identity = identityService.findById(sessionManager.getIdentityId());

	    	try {
				tokenList = twoFaService.findByIdentity(identity);
			} catch (TwoFaException e) {
				messageGenerator.addErrorMessage("messagePanel", "Error", e.toString());
				logger.debug("Exception happened", e);
			}

		}
	}
	
	public void createAuthenticatorToken() {
		if (! getReadOnly()) {
			try {
				createTokenResponse = twoFaService.createAuthenticatorToken(identity, "identity-" + identity.getId());
				tokenList = twoFaService.findByIdentity(identity);
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
			}
		}
	}

	public void createYubicoToken() {
		if (! getReadOnly()) {
			try {
				TotpCreateResponse response = twoFaService.createYubicoToken(identity, yubicoCode, "identity-" + identity.getId());

				if (response.getSuccess()) {
					String serial = response.getSerial();
					Boolean success = 
							twoFaService.checkSpecificToken(identity, serial, yubicoCode);
					if (! success) {
						// Token creating was successful, but check failed
						twoFaService.deleteToken(identity, serial, "identity-" + identity.getId());
						messageGenerator.addResolvedWarningMessage("messagePanel", "warn", "twofa_token_init_code_wrong", true);
					}
					else {
						twoFaService.initToken(identity, serial, "identity-" + identity.getId());
						messageGenerator.addInfoMessage("messagePanel", "Info", "Token " + serial + " created");
					}
					
					if (! hasActiveToken()) {
						// this was the first token. We have to set 2fa elevation
						sessionManager.setTwoFaElevation(Instant.now());
					}					

					tokenList = twoFaService.findByIdentity(identity);
				}
				else {
					messageGenerator.addResolvedWarningMessage("messagePanel", "warn", "twofa_token_failed", true);
				}
	
				PrimeFaces.current().executeScript("PF('addYubicoDlg').hide();");
				createTokenResponse = null;
				yubicoCode = "";
				
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addResolvedWarningMessage("messagePanel", "warn", "twofa_token_failed", true);
				PrimeFaces.current().executeScript("PF('addYubicoDlg').hide();");
				createTokenResponse = null;
				yubicoCode = "";
			}
		}
	}

	public void createBackupTanList() {
		if (! getReadOnly()) {
			try {
				TotpCreateResponse response = twoFaService.createBackupTanList(identity, "identity-" + identity.getId());

				if (response.getSuccess()) {
					String serial = response.getSerial();
					twoFaService.initToken(identity, serial, "identity-" + identity.getId());
					
					messageGenerator.addInfoMessage("messagePanel", "Info", "Token " + serial + " created");
					
					if (! hasActiveToken()) {
						// this was the first token. We have to set 2fa elevation
						sessionManager.setTwoFaElevation(Instant.now());
					}					

					tokenList = twoFaService.findByIdentity(identity);
					
				}
				else {
					messageGenerator.addResolvedWarningMessage("messagePanel", "warn", "twofa_token_failed", true);
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
				backupTanList = twoFaService.getBackupTanList(identity, serial, "identity-" + identity.getId());
				
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
			}
		}
	}
	
	public void checkAuthenticatorToken() {
		try {
			String serial = createTokenResponse.getSerial();
			LinotpSimpleResponse response = twoFaService.enableToken(identity, serial, "identity-" + identity.getId());

			if (response.getResult() != null && response.getResult().isStatus() && response.getResult().isValue()) {
			
				Boolean success = twoFaService.checkSpecificToken(identity, serial, totpCode);
				if (success) {
					// success, Token stays active, set correct description
					twoFaService.initToken(identity, serial, "identity-" + identity.getId());
					if (! hasActiveToken()) {
						// this was the first token. We have to set 2fa elevation
						sessionManager.setTwoFaElevation(Instant.now());
					}

					messageGenerator.addInfoMessage("messagePanel", "Info", "Token " + serial + " created");

					tokenList = twoFaService.findByIdentity(identity);
					PrimeFaces.current().executeScript("PF('addTotpDlg').hide();");
					createTokenResponse = null;
					totpCode = "";
				} 
				else {
					// wrong code, disable token
					twoFaService.disableToken(identity, serial, "identity-" + identity.getId());
					totpCode = "";
					messageGenerator.addResolvedWarningMessage("totp_messages", "warning", "twofa_token_init_code_wrong", true);
				}
			}
		} catch (TwoFaException e) {
			logger.warn("TwoFaException", e);
		}
	}

	public void enableToken(String serial) {
		if (! getReadOnly()) {
			try {
				LinotpSimpleResponse response = twoFaService.enableToken(identity, serial, "identity-" + identity.getId());
				tokenList = twoFaService.findByIdentity(identity);
				if ((response.getResult() != null) && response.getResult().isStatus() &&
						response.getResult().isValue()) {
					messageGenerator.addInfoMessage("messagePanel", "Info", "Token " + serial + " enabled");
				}
				else {
					messageGenerator.addWarningMessage("messagePanel", "Warn", "Token " + serial + " could not be enabled");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("messagePanel", "Error", e.toString());
			}
		}
	}

	public void disableToken(String serial) {
		if (! getReadOnly()) {
			try {
				TokenStatusResponse response = twoFaService.disableToken(identity, serial, "identity-" + identity.getId());
				tokenList = twoFaService.findByIdentity(identity);
				if (response.getSuccess()) {
					messageGenerator.addInfoMessage("messagePanel", "Info", "Token " + serial + " disabled");
				}
				else {
					messageGenerator.addWarningMessage("messagePanel", "Warn", "Token " + serial + " could not be disable");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("messagePanel", "Error", e.toString());
			}
		}
	}

	public void deleteToken(String serial) {
		if (! getReadOnly()) {
			try {
				LinotpSimpleResponse response = twoFaService.deleteToken(identity, serial, "identity-" + identity.getId());
				tokenList = twoFaService.findByIdentity(identity);
				if ((response.getResult() != null) && response.getResult().isStatus() &&
						response.getResult().isValue()) {
					messageGenerator.addInfoMessage("messagePanel", "Info", "Token " + serial + " deleted");
				}
				else {
					messageGenerator.addWarningMessage("messagePanel", "Warn", "Token " + serial + " could not be deleted");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("messagePanel", "Error", e.toString());
			}
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
	
	public TwoFaTokenList getTokenList() {
		return tokenList;
	}

	public TotpCreateResponse getCreateTokenResponse() {
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

	private Boolean hasActiveToken() {
		for (GenericTwoFaToken token : tokenList) {
			if (token.getIsactive()) {
				/*
				 * filter token, that are not initialized
				 */
				if (token.getDescription() != null && token.getDescription().contains("INIT")) {
					return false;
				}
				return true;
			}
		}
		
		return false;
	}

}
