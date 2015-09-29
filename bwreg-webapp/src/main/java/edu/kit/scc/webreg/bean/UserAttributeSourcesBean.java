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

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.service.ASUserAttrService;
import edu.kit.scc.webreg.service.AttributeSourceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.session.SessionManager;

@ManagedBean
@ViewScoped
public class UserAttributeSourcesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private UserEntity user;
	
	private List<ASUserAttrEntity> userAttrList;
	private ASUserAttrEntity selectedUserAttr;
	private AttributeSourceEntity selectedAttributeSource;
	
	@Inject
	private UserService userService;
    
    @Inject
    private ASUserAttrService asUserAttrService;

    @Inject
    private AttributeSourceService attributeSourceService;
    
    @Inject 
    private SessionManager sessionManager;

	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
	    	user = userService.findById(sessionManager.getUserId());
	    	userAttrList = asUserAttrService.findForUser(user);
		}
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public List<ASUserAttrEntity> getUserAttrList() {
		return userAttrList;
	}

	public void setUserAttrList(List<ASUserAttrEntity> userAttrList) {
		this.userAttrList = userAttrList;
	}

	public ASUserAttrEntity getSelectedUserAttr() {
		return selectedUserAttr;
	}

	public void setSelectedUserAttr(ASUserAttrEntity selectedUserAttr) {
		selectedUserAttr = asUserAttrService.findByIdWithAttrs(selectedUserAttr.getId(), "values");
		selectedAttributeSource = attributeSourceService.findByIdWithAttrs(
				selectedUserAttr.getAttributeSource().getId(), "attributeSourceServices");
		this.selectedUserAttr = selectedUserAttr;
	}

	public AttributeSourceEntity getSelectedAttributeSource() {
		return selectedAttributeSource;
	}
}
