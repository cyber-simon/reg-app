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
package edu.kit.scc.webreg.bean.admin;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.GenericSortOrder;
import edu.kit.scc.webreg.dao.ops.MultipathOrPredicate;
import edu.kit.scc.webreg.dao.ops.PathObjectValue;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class FindIdentityBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
    @Inject
    private UserService userService;

 	private UserEntity selectedUser;

	public void preRenderView(ComponentSystemEvent ev) {
	}

	public List<UserEntity> completeUser(String part) {
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("eppn", new MultipathOrPredicate(
				new PathObjectValue("eppn", part),
				new PathObjectValue("surName", part),
				new PathObjectValue("givenName", part)
		));
		return userService.findAllPaging(0, 10, "eppn", GenericSortOrder.ASC, filterMap, null);
	}


	public String searchUser() {
		if (selectedUser == null) {
			messageGenerator.addErrorMessage("Error", "User not found");
			return null;
		}
		else {
			try {
				FacesContext.getCurrentInstance().getExternalContext().redirect("show-admin-identity.xhtml?id=" + selectedUser.getIdentity().getId());
			} catch (IOException e) {
				messageGenerator.addErrorMessage("Error", "Can't redirect to details page");
			}
			return "";
		}
	}

	public UserEntity getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(UserEntity selectedUser) {
		this.selectedUser = selectedUser;
	}

}
