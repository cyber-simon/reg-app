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

import com.nimbusds.openid.connect.sdk.claims.ClaimsSet;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.service.GroupService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.session.SessionManager;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

@ManagedBean
@ViewScoped
public class UserPropertiesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private IdentityEntity identity;
	private List<UserEntity> userList;
	private UserEntity user;
	
	private SamlIdpMetadataEntity idpEntity;

	private List<RoleEntity> roleList;

	private List<GroupEntity> groupList;
	
	private ClaimsSet claims;
	
	@Inject
	private UserService userService;
    
	@Inject
	private IdentityService identityService;

	@Inject
	private RoleService roleService;
	
	@Inject
	private GroupService groupService;
	
    @Inject 
    private SessionManager sessionManager;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (identity == null) {
			identity = identityService.findById(sessionManager.getIdentityId());
			userList = userService.findByIdentity(identity);
			user = userService.findByIdWithStore(userList.get(0).getId());
	    	roleList = roleService.findByUser(user);
	    	groupList = groupService.findByUser(user);
	    	
	    	if (user instanceof SamlUserEntity) {
	    		idpEntity = ((SamlUserEntity) user).getIdp();
	    	}
	    	else if (user instanceof OidcUserEntity) {
	    		JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
				try {
		    		JSONObject jo = parser.parse(user.getAttributeStore().get("claims"), JSONObject.class);
		    		claims = new ClaimsSet(jo);
				} catch (ParseException e) {
					System.out.println("" + e);
				}
	    	}
		}
	}
	
	public SamlIdpMetadataEntity getIdpEntity() {
		return idpEntity;
	}

	public List<RoleEntity> getRoleList() {
		return roleList;
	}

	public List<GroupEntity> getGroupList() {
		return groupList;
	}

	public IdentityEntity getIdentity() {
		return identity;
	}

	public List<UserEntity> getUserList() {
		return userList;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public ClaimsSet getClaims() {
		return claims;
	}
}
