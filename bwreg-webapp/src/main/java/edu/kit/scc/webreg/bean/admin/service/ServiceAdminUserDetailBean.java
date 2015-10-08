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

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;

@ManagedBean
@ViewScoped
public class ServiceAdminUserDetailBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
    @Inject
    private RegistryService service;

    @Inject
    private AuthorizationBean authBean;

    @Inject
    private RegisterUserService registerUserService;
  
    @Inject
    private SessionManager sessionManager;
    
    private RegistryEntity entity;
    
    private Long id;

	public void preRenderView(ComponentSystemEvent ev) {
		entity = service.findByIdWithAgreements(id);
		
		if (! (authBean.isUserServiceAdmin(entity.getService()) || 
				authBean.isUserServiceHotline(entity.getService())))
			throw new NotAuthorizedException("Nicht autorisiert");		
	}

	public void reconsiliation() {
		logger.debug("Manual quick recon for Account {} Service {}", entity.getUser().getEppn(), entity.getService().getName());
		try {
			registerUserService.reconsiliation(entity, false, "service-admin-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.error("An error occured", e);
		}
	}
	
	public void fullReconsiliation() {
		logger.debug("Manual full recon for Account {} Service {}", entity.getUser().getEppn(), entity.getService().getName());
		try {
			registerUserService.reconsiliation(entity, true, "service-admin-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.error("An error occured", e);
		}
	}
	
	public void deregister() {
		try {
			logger.info("Deregister registry {} via AdminRegistry page", entity.getId());
			registerUserService.deregisterUser(entity, "service-admin-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.warn("Could not deregister User", e);
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RegistryEntity getEntity() {
		return entity;
	}

	public void setEntity(RegistryEntity entity) {
		this.entity = entity;
	}

}
