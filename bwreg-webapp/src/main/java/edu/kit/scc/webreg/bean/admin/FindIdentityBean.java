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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.contains;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.or;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;
import static edu.kit.scc.webreg.entity.UserEntity_.eppn;
import static edu.kit.scc.webreg.entity.UserEntity_.givenName;
import static edu.kit.scc.webreg.entity.UserEntity_.surName;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@Named
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
		RqlExpression filterBy = or(contains(eppn, part), contains(surName, part), contains(givenName, part));
		return userService.findAll(withLimit(10), ascendingBy(eppn), filterBy);
	}

	public String searchUser() {
		if (selectedUser == null) {
			messageGenerator.addErrorMessage("Error", "User not found");
			return null;
		} else {
			try {
				FacesContext.getCurrentInstance().getExternalContext()
						.redirect("show-admin-identity.xhtml?id=" + selectedUser.getIdentity().getId());
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
