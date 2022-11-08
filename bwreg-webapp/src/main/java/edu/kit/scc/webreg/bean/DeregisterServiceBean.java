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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.identity.IdentityService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;
import edu.kit.scc.webreg.util.BBCodeConverter;
import edu.kit.scc.webreg.util.ViewIds;

@Named
@ViewScoped
public class DeregisterServiceBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DeregisterServiceBean.class);
	
	private RegistryEntity registry;
	private IdentityEntity identityEntity;
	
	private Long id;
	private Boolean initialzed = false;
	
    @Inject
    private RegistryService registryService;

    @Inject 
    private SessionManager sessionManager;
 
    @Inject
    private RegisterUserService registerUserService;

	@Inject
	private IdentityService identityService;
	
	@Inject
	private BBCodeConverter bbCodeConverter;

	private String deregisterTextBB;

	public void preRenderView(ComponentSystemEvent ev) {
	   	if (! initialzed) {
			registry = registryService.findById(id);
			
			if (registry == null)
				throw new NotAuthorizedException("No such item");
	
			identityEntity = identityService.findById(sessionManager.getIdentityId());
	
			if (! registry.getIdentity().getId().equals(identityEntity.getId()))
				throw new NotAuthorizedException("Not authorized to view this item");
			
			deregisterTextBB = bbCodeConverter.convert(registry.getService().getDeregisterText());

			initialzed = true;
	   	}
	}

    public String deregisterUser() {
    	
    	logger.debug("user {} wants to deregister to service {} using bean {}", new Object[] {
    			registry.getUser().getEppn(), registry.getService().getName(), registry.getRegisterBean()
    	});
    	
    	try {
    		registerUserService.deregisterUser(registry, "user-self", "user-self-via-web");
    		sessionManager.setUnregisteredServiceCreated(null);
    	} catch (RegisterException e) {
    		logger.warn("Deregister failed!", e);
		}

    	return ViewIds.INDEX_USER + "?faces-redirect=true";
    }
    
	public UserEntity getUser() {
		return registry.getUser();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public ServiceEntity getService() {
		return registry.getService();
	}

	public String getDeregisterTextBB() {
		return deregisterTextBB;
	}
}
