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
package edu.kit.scc.webreg.bean.tadm;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.contains;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.or;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;
import static edu.kit.scc.webreg.entity.UserEntity_.eppn;
import static edu.kit.scc.webreg.entity.UserEntity_.givenName;
import static edu.kit.scc.webreg.entity.UserEntity_.surName;

import java.io.Serializable;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.token.TokenStatusResponse;
import edu.kit.scc.webreg.service.twofa.token.TwoFaTokenList;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
@ViewScoped
public class TokenAdminIndexBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserService userService;

	@Inject
	private TwoFaService twoFaService;

	@Inject
	private SessionManager session;

	@Inject
	private FacesMessageGenerator messageGenerator;

	@Inject
	private AuthorizationBean authBean;

	private UserEntity selectedUser;
	private TwoFaTokenList userTokenList;

	public void preRenderView(ComponentSystemEvent ev) {

	}

	public void searchToken() {
		if (selectedUser == null) {
			messageGenerator.addErrorMessage("Error", "User not found");
			userTokenList = null;
		} else {
			try {
				userTokenList = twoFaService.findByIdentity(selectedUser.getIdentity());

				if (userTokenList.getReadOnly()) {
					messageGenerator.addWarningMessage("Warning", "User is in read only realm");
				}

				if (userTokenList.getAdminRole() != null && (!authBean.isUserInRole(userTokenList.getAdminRole()))) {
					messageGenerator.addWarningMessage("Warning", "You are not admin for this realm");
					userTokenList = null;
				}
			} catch (TwoFaException e) {
				messageGenerator.addErrorMessage("Error", "Can't load token list for user: " + e.getMessage());
			}
		}
	}

	public List<UserEntity> completeUser(String part) {
		RqlExpression filterBy = or(contains(eppn, part), contains(surName, part), contains(givenName, part));
		return userService.findAll(withLimit(10), ascendingBy(eppn), filterBy);
	}

	public void enableToken(String serial) {
		if (!getReadOnly()) {
			try {
				TokenStatusResponse response = twoFaService.enableToken(selectedUser.getIdentity(), serial,
						"identity-" + session.getIdentityId());
				userTokenList = twoFaService.findByIdentity(selectedUser.getIdentity());
				if (response.getSuccess()) {
					messageGenerator.addInfoMessage("Info", "Token " + serial + " enabled");
				} else {
					messageGenerator.addWarningMessage("Warn", "Token " + serial + " could not be enabled");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("Error", e.toString());
			}
		}
	}

	public void disableToken(String serial) {
		if (!getReadOnly()) {
			try {
				TokenStatusResponse response = twoFaService.disableToken(selectedUser.getIdentity(), serial,
						"identity-" + session.getIdentityId());
				userTokenList = twoFaService.findByIdentity(selectedUser.getIdentity());
				if (response.getSuccess()) {
					messageGenerator.addInfoMessage("Info", "Token " + serial + " disabled");
				} else {
					messageGenerator.addWarningMessage("Warn", "Token " + serial + " could not be disabled");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("Error", e.toString());
			}
		}
	}

	public void resetFailcounter(String serial) {
		if (!getReadOnly()) {
			try {
				TokenStatusResponse response = twoFaService.resetFailcounter(selectedUser.getIdentity(), serial,
						"identity-" + session.getIdentityId());
				userTokenList = twoFaService.findByIdentity(selectedUser.getIdentity());
				if (response.getSuccess()) {
					messageGenerator.addInfoMessage("Info", "Token " + serial + " failcounter reset");
				} else {
					messageGenerator.addWarningMessage("Warn",
							"Token " + serial + " failcounter could not be resetted");
				}
			} catch (TwoFaException e) {
				logger.warn("TwoFaException", e);
				messageGenerator.addErrorMessage("Error", e.toString());
			}
		}
	}

	public Boolean getReadOnly() {
		return userTokenList == null || userTokenList.getReadOnly();
	}

	public TwoFaTokenList getUserTokenList() {
		return userTokenList;
	}

	public UserEntity getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserEntity selectedUser) {
		this.selectedUser = selectedUser;
	}

}
