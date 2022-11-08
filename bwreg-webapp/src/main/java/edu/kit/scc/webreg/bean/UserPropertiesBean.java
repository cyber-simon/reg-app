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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;

import com.nimbusds.openid.connect.sdk.claims.ClaimsSet;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.entity.project.ProjectMembershipEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.project.ProjectService;
import edu.kit.scc.webreg.session.SessionManager;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@Named
@ViewScoped
public class UserPropertiesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private IdentityEntity identity;
	private List<UserEntity> userList;
	private UserEntity user;
	
	private List<RoleEntity> roleList;

	private List<GroupEntity> groupList;

    private List<ProjectMembershipEntity> projectMemberList;

	private ClaimsSet claims;
	
	@Inject
	private Logger logger;
	
	@Inject
	private UserService userService;
    
	@Inject
	private IdentityService identityService;

	@Inject
	private RoleService roleService;
	
	@Inject
	private GroupService groupService;

 	@Inject
 	private ProjectService projectService;

    @Inject 
    private SessionManager sessionManager;
    
	public void preRenderView(ComponentSystemEvent ev) {
	    	
	}
	
	public List<RoleEntity> getRoleList() {
    	roleList = roleService.findByUser(getUser());
		return roleList;
	}

	public List<GroupEntity> getGroupList() {
    	groupList = groupService.findByUser(getUser());
		return groupList;
	}

	public IdentityEntity getIdentity() {
		if (identity == null) {
			identity = identityService.findById(sessionManager.getIdentityId());
		}
		return identity;
	}

	public List<UserEntity> getUserList() {
		if (userList == null) {
			userList = userService.findByIdentity(getIdentity());
		}
		return userList;
	}

	public UserEntity getUser() {
		if (user == null) {
			if (sessionManager.getLoggedInUserList().size() > 0) {
				user = userService.findByIdWithStore(sessionManager.getLoggedInUserList().iterator().next());
			}
			else {
				user = userService.findByIdWithStore(getUserList().get(0).getId());
			}
		}
		return user;
	}

	public void setUser(UserEntity user) {
		if (user != null && (! user.equals(this.user))) {
			this.user = userService.findByIdWithStore(user.getId());
			claims = null;
		}
	}

	public ClaimsSet getClaims() {
		if (claims == null) {
	    	if (getUser() instanceof OidcUserEntity) {
	    		JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
				try {
					if (getUser().getAttributeStore().containsKey("claims")) {
			    		JSONObject jo = parser.parse(getUser().getAttributeStore().get("claims"), JSONObject.class);
			    		claims = new ClaimsSet(jo);
					}
				} catch (ParseException e) {
					logger.warn("Wrong JSON in claims attribute", e);
				}
	    	}
		}
		return claims;
	}

	public List<ProjectMembershipEntity> getProjectMemberList() {
		if (projectMemberList == null) {
			projectMemberList = projectService.findByIdentity(getIdentity());
		}
		return projectMemberList;
	}

}
