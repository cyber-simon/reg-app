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

import static edu.kit.scc.webreg.dao.ops.PaginateBy.unlimited;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.SortBy.ascendingBy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import edu.kit.scc.webreg.entity.SamlUserEntity_;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;

@Named
@ViewScoped
public class ShowAdminIdentityBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserService userService;

	@Inject
	private IdentityService service;

	private IdentityEntity identity;
	private List<UserEntity> userList;
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = service.fetch(id);
		}
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = userService.findAllEagerly(unlimited(), Arrays.asList(ascendingBy(UserEntity_.id)),
					equal(UserEntity_.identity, getIdentity()), UserEntity_.genericStore, UserEntity_.attributeStore,
					SamlUserEntity_.idp);
		}
		return userList;
	}

}
