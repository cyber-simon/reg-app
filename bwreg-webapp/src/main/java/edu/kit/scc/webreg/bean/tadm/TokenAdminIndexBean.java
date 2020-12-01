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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.GenericSortOrder;
import edu.kit.scc.webreg.dao.ops.MultipathOrPredicate;
import edu.kit.scc.webreg.dao.ops.PathObjectValue;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.twofa.TwoFaException;
import edu.kit.scc.webreg.service.twofa.TwoFaService;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpSimpleResponse;
import edu.kit.scc.webreg.service.twofa.linotp.LinotpTokenResultList;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
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
 	private LinotpTokenResultList userTokenList;
 	
	public void preRenderView(ComponentSystemEvent ev) {
		
	}

	public void searchToken() {
		if (selectedUser == null) {
			messageGenerator.addErrorMessage("Error", "User not found");
			userTokenList = null;
		}
		else {
			try {
				userTokenList = twoFaService.findByIdentity(selectedUser.getIdentity());
				
				if (userTokenList.getReadOnly()) {
					messageGenerator.addWarningMessage("Warning", "User is in read only realm");
				}
				
				if (userTokenList.getAdminRole() != null && 
						(! authBean.isUserInRole(userTokenList.getAdminRole()))) {
					messageGenerator.addWarningMessage("Warning", "You are not admin for this realm");
					userTokenList = null;
				}
			} catch (TwoFaException e) {
				messageGenerator.addErrorMessage("Error", "Can't load token list for user: " + e.getMessage());
			}
		}
	}
	
	public List<UserEntity> completeUser(String part) {
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("eppn", new MultipathOrPredicate(
				new PathObjectValue("eppn", part),
				new PathObjectValue("surName", part),
				new PathObjectValue("givenName", part)
		));
		return userService.findAllPaging(0, 10, "eppn", GenericSortOrder.ASC, filterMap);
	}

	public void enableToken(String serial) {
		if (! getReadOnly()) {
			try {
				LinotpSimpleResponse response = twoFaService.enableToken(selectedUser.getIdentity(), serial, "identity-" + session.getIdentityId());
				userTokenList = twoFaService.findByIdentity(selectedUser.getIdentity());
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
				LinotpSimpleResponse response = twoFaService.disableToken(selectedUser.getIdentity(), serial, "identity-" + session.getIdentityId());
				userTokenList = twoFaService.findByIdentity(selectedUser.getIdentity());
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
	
	public Boolean getReadOnly() {
		if (userTokenList != null)
			return userTokenList.getReadOnly();
		else
			return true;
	}

	public LinotpTokenResultList getUserTokenList() {
		return userTokenList;
	}

	public UserEntity getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserEntity selectedUser) {
		this.selectedUser = selectedUser;
	}

}
