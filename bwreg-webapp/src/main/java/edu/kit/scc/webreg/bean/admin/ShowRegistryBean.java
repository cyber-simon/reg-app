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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.ViewIds;

@ManagedBean
@ViewScoped
public class ShowRegistryBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private RegistryService registryService;

	@Inject
	private RegisterUserService registerUserService;
	
	@Inject
	private SessionManager sessionManager;
	
	private RegistryEntity registry;

	private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		if (registry == null) {
			registry = registryService.findByIdWithAgreements(id);
		}
	}
	
	public void deregister() {
		try {
			logger.info("Deregister registry {} via AdminRegistry page", registry.getId());
			registerUserService.deregisterUser(registry, "user-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.warn("Could not deregister User", e);
		}
	}

	public String purgeRegistry() {
		long userId = registry.getUser().getId();

		logger.info("Purging registry {} via AdminRegistry page", registry.getId());
		
		try {
			registerUserService.purge(registry, "user-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.warn("Could not purge Registry", e);
		}
		
		return ViewIds.SHOW_USER + "?id=" + userId + "&faces-redirect=true";
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RegistryEntity getRegistry() {
		return registry;
	}

	public void setRegistry(RegistryEntity registry) {
		this.registry = registry;
	}
	
}
