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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.opensaml.saml2.core.Attribute;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserRoleEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.UserUpdateService;
import edu.kit.scc.webreg.util.SessionManager;

@ManagedBean
@ViewScoped
public class ShowUserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserService userService;

	@Inject
	private RoleService roleService;
	
	@Inject
	private UserUpdateService userUpdateService;

	@Inject
	private RegistryService registryService;
	
	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@Inject
	private SessionManager sessionManager;
	
	private UserEntity user;

	private DualListModel<RoleEntity> roleList;
	
	private Map<String, Attribute> attributeMap;
	
	private List<RegistryEntity> registryList;
	
	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (user == null) {
			user = userService.findByIdWithAll(id);
			roleList = new DualListModel<RoleEntity>();
			List<RoleEntity> targetList = new ArrayList<RoleEntity>(user.getRoles().size());
			for (UserRoleEntity userRole : user.getRoles())
				targetList.add(userRole.getRole());
			roleList.setTarget(targetList);
			List<RoleEntity> sourceList = roleService.findAll();
			sourceList.removeAll(targetList);
			roleList.setSource(sourceList);
			registryList = registryService.findByUser(user);
		}
	}

	public void onTransfer(TransferEvent event) {
		user = userService.findByIdWithAll(id);
		if (event.isAdd()) {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				roleService.addUserToRole(user, role.getName());
			}
		}
		else {
			for (Object o : event.getItems()) {
				RoleEntity role = (RoleEntity) o;
				roleService.removeUserFromRole(user, role.getName());
			}
		}
		user = userService.findByIdWithAll(id);
	}
	
	public void updateFromIdp() {
		user = userService.findByIdWithAll(id);
		logger.info("Trying user update for {}", user.getEppn());

		try {
			userUpdateService.updateUserFromIdp(user);
		} catch (RegisterException e) {
			logger.info("Exception while Querying IDP: {}", e.getMessage());
			if (e.getCause() != null) {
				logger.info("Cause is: {}", e.getCause().getMessage());
				if (e.getCause().getCause() != null) {
					logger.info("Inner Cause is: {}", e.getCause().getCause().getMessage());
				}
			}
		}
	}

	public void checkAllRegistries() {
		user = userService.findByIdWithAll(id);
		logger.info("Trying to check all registries for user {}", user.getEppn());
		
		List<RegistryEntity> tempRegistryList = new ArrayList<RegistryEntity>();
		for (RegistryEntity registry : registryList) {
			if (RegistryStatus.ACTIVE.equals(registry.getRegistryStatus()) ||
					RegistryStatus.LOST_ACCESS.equals(registry.getRegistryStatus())) {
				tempRegistryList.add(registry);
			}
		}
		knowledgeSessionService.checkRules(tempRegistryList, user, "user-" + sessionManager.getUserId(), false);
	}
	
	public UserEntity getEntity() {
		return user;
	}

	public void setEntity(UserEntity user) {
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		if (id != this.id) {
			user = null;
			attributeMap = null;
		}
		this.id = id;
	}
	
	public List<Attribute> getAttributeValues() {
		if (attributeMap != null)
			return new ArrayList<Attribute>(attributeMap.values());
		else
			return null;
	}

	public Map<String, Attribute> getAttributeMap() {
		return attributeMap;
	}

	public DualListModel<RoleEntity> getRoleList() {
		return roleList;
	}

	public void setRoleList(DualListModel<RoleEntity> roleList) {
		this.roleList = roleList;
	}

	public List<RegistryEntity> getRegistryList() {
		return registryList;
	}

	public void setRegistryList(List<RegistryEntity> registryList) {
		this.registryList = registryList;
	}
	
}
