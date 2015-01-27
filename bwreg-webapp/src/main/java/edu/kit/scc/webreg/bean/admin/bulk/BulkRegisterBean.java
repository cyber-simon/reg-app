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
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

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

@ManagedBean
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
	
	private String eppnField = "ls1947@kit.edu\nugcne@student.kit.edu";
	
	private List<RegisterUser> registerUserList;

	private RegisterUser[] selectedUsers;
	
	private List<ServiceEntity> serviceList;
	
	private ServiceEntity selectedService;
	
	private LazyDataModel<UserEntity> userList;
	
	private UserEntity[] insertUser;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceList == null) 
			serviceList = serviceSerivce.findAll();
	}
	
	public void fillTable() {
		if (registerUserList == null)
			registerUserList = new ArrayList<RegisterUser>();

		try {
			BufferedReader reader = new BufferedReader(new StringReader(eppnField));
			String line;
			while ((line = reader.readLine()) != null) {
				String eppn = line.trim();
				RegisterUser registerUser = new RegisterUser();
				registerUser.setEppn(eppn);
				if (! registerUserList.contains(registerUser)) {
					registerUserList.add(registerUser);
					logger.debug("Adding user {} for registry", eppn);
				}
			}
			
		} catch (IOException e) {
			logger.error("StringReader broke down", e);
		} 
	}

	public void insert() {
		if (registerUserList == null)
			registerUserList = new ArrayList<RegisterUser>();

		for (UserEntity u : insertUser) {
			RegisterUser registerUser = new RegisterUser();
			registerUser.setEppn(u.getEppn());
			if (! registerUserList.contains(registerUser)) {
				registerUserList.add(registerUser);
				logger.debug("Adding user {} for registry", u.getEppn());
			}
		}
	}

	public void processSelected() {
		ServiceEntity service = serviceSerivce.findWithPolicies(selectedService.getId());
		
		for (RegisterUser registerUser : selectedUsers) {
			try {
				logger.debug("Processing user {} for service {}", registerUser.getEppn(), selectedService.getName());
				UserEntity userEntity = userService.findByEppn(registerUser.getEppn());
			
				if (userEntity == null) {
					registerUser.setStatus("User unkown");
					continue;
				}
				
				RegistryEntity registry = registryService.findByServiceAndUserAndStatus(
						selectedService, userEntity, RegistryStatus.ACTIVE);
				
				if (registry != null) {
					registerUser.setStatus("User already registered");
					continue;
				}
				
	    		registerUserService.registerUser(userEntity, service, "bulk-register");
				registerUser.setStatus("Successfully registered");

			} catch (Exception e) {
				logger.warn("Register failed", e);
				registerUser.setStatus("Fehler: " + e.getMessage());
			}
		}
	}

	public String getEppnField() {
		return eppnField;
	}

	public void setEppnField(String eppnField) {
		this.eppnField = eppnField;
	}

	public List<RegisterUser> getRegisterUserList() {
		return registerUserList;
	}

	public void setRegisterUserList(List<RegisterUser> registerUserList) {
		this.registerUserList = registerUserList;
	}

	public RegisterUser[] getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(RegisterUser[] selectedUsers) {
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

	public UserEntity[] getInsertUser() {
		return insertUser;
	}

	public void setInsertUser(UserEntity[] insertUser) {
		this.insertUser = insertUser;
	}

	
}
