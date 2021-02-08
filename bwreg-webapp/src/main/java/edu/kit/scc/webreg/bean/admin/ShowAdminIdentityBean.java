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

import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class ShowAdminIdentityBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
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
			identity = service.findById(id);
		}
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = userService.findByIdentity(getIdentity());
		}
		return userList;
	}

}
