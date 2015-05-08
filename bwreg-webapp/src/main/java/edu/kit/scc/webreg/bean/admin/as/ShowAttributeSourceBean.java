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
package edu.kit.scc.webreg.bean.admin.as;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.AttributeSourceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.AttributeSourceQueryService;

@ManagedBean
@ViewScoped
public class ShowAttributeSourceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private AttributeSourceService service;

	@Inject
	private UserService userService;
	
	@Inject
	private AttributeSourceQueryService asQueryService;

	@Inject
	private Logger logger;
	
	private AttributeSourceEntity entity;
	
	private Long id;

	private String testUsername;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (entity == null) {
			entity = service.findByIdWithAttrs(id, "asProps");
		}
	}
		
	public void testSource() {
		UserEntity user = userService.findByEppn(testUsername);
		
		if (user == null) {
			logger.info("User {} not found", testUsername);
			return;
		}
		
		try {
			asQueryService.updateUserAttributes(user, entity, "test");
		} catch (UserUpdateException e) {
			logger.info("Exception!", e);
		}
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AttributeSourceEntity getEntity() {
		return entity;
	}

	public void setEntity(AttributeSourceEntity entity) {
		this.entity = entity;
	}

	public String getTestUsername() {
		return testUsername;
	}

	public void setTestUsername(String testUsername) {
		this.testUsername = testUsername;
	}
}
