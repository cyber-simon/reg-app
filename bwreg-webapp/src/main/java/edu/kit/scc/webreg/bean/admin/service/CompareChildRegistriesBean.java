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
package edu.kit.scc.webreg.bean.admin.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;

@ManagedBean
@ViewScoped
public class CompareChildRegistriesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private SessionManager sessionManager;
	
	@Inject
	private ServiceService service;
	
	@Inject
	private RegistryService registryService;
	
	@Inject
	private RegisterUserService registerUserService;
	
	private ServiceEntity entity;
	private List<RegistryEntity> registryList;
	
	private List<ServiceEntity> childServiceList;
	private Map<ServiceEntity, List<RegistryEntity>> registriesMap;
	
	private Long id;

	private Boolean initialized = false;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (! initialized) {
			logger.debug("Load service for id {}", id);
			entity = service.findById(id);

			logger.debug("Loading active registries for service {}", entity.getName());
			registryList = registryService.findByServiceAndNotStatus(entity, 
					RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
					
			childServiceList = service.findByParentService(entity);
			registriesMap = new HashMap<ServiceEntity, List<RegistryEntity>>(childServiceList.size());
			
			initialized = true;
		}
	}

	public void loadRegistries(ServiceEntity service) {
		logger.debug("Loading active registries for child service {}", service.getName());
		registriesMap.put(service,
				registryService.findByServiceAndNotStatus(service, 
						RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED)
				);
		logger.debug("Done Loading active registries for child service {}", service.getName());
	}

	public List<RegistryEntity> compareMissing(ServiceEntity service) {
		Set<UserEntity> userList = new HashSet<UserEntity>();
		for (RegistryEntity registry : registryList) {
			userList.add(registry.getUser());
		}
		
		List<RegistryEntity> missingList = new ArrayList<RegistryEntity>();
		for (RegistryEntity registry : registriesMap.get(service)) {
			if (! userList.contains(registry.getUser())) {
				missingList.add(registry);
			}
		}
		return missingList;
	}
	
	public void registerMissing(ServiceEntity service) {
		List<RegistryEntity> missingList = compareMissing(service);
		logger.info("Will register {} missing users for service {}", missingList.size(), entity.getName());
		
		for (RegistryEntity registry : missingList) {
			if (! UserStatus.ACTIVE.equals(registry.getUser().getUserStatus())) {
				logger.debug("Skipping registration of user {} to service {}, user is {}", registry.getUser().getEppn(), entity.getName(), registry.getUser().getUserStatus());
			}
			else {
				logger.debug("Starting registration of user {} to service {}", registry.getUser().getEppn(), entity.getName());
				List<RegistryEntity> tempRegistryList = registryService.findByServiceAndUserAndNotStatus(entity, 
						registry.getUser(), RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
				
				if (tempRegistryList.size() == 0) {
					try {
						registerUserService.registerUser(registry.getUser(), entity, "admin-" + sessionManager.getUserId(), false);
					} catch (RegisterException e) {
						logger.warn("Registration failed", e);
					}
				}
				else {
					logger.info("User {} is seems already to be registered with service {}", registry.getUser().getEppn(), entity);
				}
			}
		}
	}
	
	public List<RegistryEntity> getRegistries(ServiceEntity service) {
		return registriesMap.get(service);
	}
	
	public ServiceEntity getEntity() {
		return entity;
	}

	public void setEntity(ServiceEntity entity) {
		this.entity = entity;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ServiceEntity> getChildServiceList() {
		return childServiceList;
	}

	public void setChildServiceList(List<ServiceEntity> childServiceList) {
		this.childServiceList = childServiceList;
	}

	public List<RegistryEntity> getRegistryList() {
		return registryList;
	}

	public void setRegistryList(List<RegistryEntity> registryList) {
		this.registryList = registryList;
	}

	public Map<ServiceEntity, List<RegistryEntity>> getRegistriesMap() {
		return registriesMap;
	}

	public void setRegistriesMap(Map<ServiceEntity, List<RegistryEntity>> registriesMap) {
		this.registriesMap = registriesMap;
	}
}
