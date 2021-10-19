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
package edu.kit.scc.webreg.bean.admin.bulk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.UserService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

@Named
@ViewScoped
public class BulkRegisterBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserService userService;
	
	@Inject
	private ServiceService serviceSerivce;

	@Inject
	private RegistryService registryService;

	@Inject
	private RegisterUserService registerUserService;
	
	private String eppnField = "user1@org2.de\nuser2@org1.edu";
	
	private Set<RegisterUser> registerUserList;

	private List<RegisterUser> selectedUsers;
	
	private List<ServiceEntity> serviceList;
	
	private ServiceEntity selectedService;
	
	private LazyDataModel<UserEntity> userList;
	
	private List<UserEntity> insertUser;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceList == null) 
			serviceList = serviceSerivce.findAll();
	}
	
	public void fillTable() {
		if (registerUserList == null)
			registerUserList = new HashSet<RegisterUser>();

		try {
			BufferedReader reader = new BufferedReader(new StringReader(eppnField));
			String line;
			while ((line = reader.readLine()) != null) {
				String eppn = line.trim();
				List<UserEntity> ul = userService.findByEppn(eppn);
				for (UserEntity u : ul) {
					logger.debug("Adding user {} for registry", u.getId());
					registerUserList.add(new RegisterUser(u));
				}
			}
			
		} catch (IOException e) {
			logger.error("StringReader broke down", e);
		} 
	}

	public void insert() {
		if (registerUserList == null)
			registerUserList = new HashSet<RegisterUser>();

		for (UserEntity u : insertUser) {
			registerUserList.add(new RegisterUser(u));
			logger.debug("Adding user {} for registry", u.getEppn());
		}
	}

	public void processSelected() {
		ServiceEntity service = serviceSerivce.findWithPolicies(selectedService.getId());
		
		for (RegisterUser ru : selectedUsers) {
			UserEntity u = ru.getUser();
			try {
				logger.debug("Processing user {} for service {}", u.getId(), selectedService.getName());
				
				RegistryEntity registry = registryService.findByServiceAndUserAndStatus(
						selectedService, u, RegistryStatus.ACTIVE);
				
				if (registry != null) {
					ru.setStatus("User already registered");
					continue;
				}
				
	    		registerUserService.registerUser(u, service, "bulk-register");
				ru.setStatus("Successfully registered");

			} catch (Exception e) {
				logger.warn("Register failed", e);
				ru.setStatus("Fehler: " + e.getMessage());
			}
		}
	}

	public String getEppnField() {
		return eppnField;
	}

	public void setEppnField(String eppnField) {
		this.eppnField = eppnField;
	}

	public Set<RegisterUser> getRegisterUserList() {
		return registerUserList;
	}

	public void setRegisterUserList(Set<RegisterUser> registerUserList) {
		this.registerUserList = registerUserList;
	}

	public List<RegisterUser> getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List<RegisterUser> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

	public List<ServiceEntity> getServiceList() {
		return serviceList;
	}

	public void setServiceList(List<ServiceEntity> serviceList) {
		this.serviceList = serviceList;
	}

	public ServiceEntity getSelectedService() {
		return selectedService;
	}

	public void setSelectedService(ServiceEntity selectedService) {
		this.selectedService = selectedService;
	}

	public LazyDataModel<UserEntity> getUserList() {
		if (userList == null)
			userList = new GenericLazyDataModelImpl<UserEntity, UserService, Long>(userService);
		return userList;
	}

	public List<UserEntity> getInsertUser() {
		return insertUser;
	}

	public void setInsertUser(List<UserEntity> insertUser) {
		this.insertUser = insertUser;
	}

	
}
