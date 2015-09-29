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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.Theme;
import edu.kit.scc.webreg.util.ThemeSwitcherBean;

@ManagedBean
@ViewScoped
public class UserPropertiesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserEntity user;
	
	private SamlIdpMetadataEntity idpEntity;

	private List<RoleEntity> roleList;

	private List<GroupEntity> groupList;
	
	private String theme;
	
	@Inject
	private UserService userService;
	
	@Inject
	private RoleService roleService;
	
	@Inject
	private GroupService groupService;
	
    @Inject 
    private SessionManager sessionManager;
    
    @Inject
    private ThemeSwitcherBean themeSwitcherBean;

	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
	    	user = userService.findByIdWithStore(sessionManager.getUserId());
	    	roleList = roleService.findByUser(user);
	    	groupList = groupService.findByUser(user);
	    	theme = sessionManager.getTheme();
	    	
			idpEntity = user.getIdp();
		}
	}
	
	public SamlIdpMetadataEntity getIdpEntity() {
		return idpEntity;
	}

	public UserEntity getUser() {
		return user;
	}

	public List<RoleEntity> getRoleList() {
		return roleList;
	}

    public List<Theme> getThemeList() {
        return themeSwitcherBean.getThemeList();
    }

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public void saveTheme() {
		user.setTheme(theme);
		userService.save(user);
		sessionManager.setTheme(theme);
	}

	public List<GroupEntity> getGroupList() {
		return groupList;
	}
}
