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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.session.SessionManager;

@ManagedBean
@ViewScoped
public class ServiceAdminUserListDeproBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
    @Inject
    private RegistryService service;

    @Inject
    private RegisterUserService registerUserService;
    
    @Inject
    private ServiceService serviceService;

    @Inject
    private AuthorizationBean authBean;
    
    @Inject
    private SessionManager sessionManager;
    
    private ServiceEntity serviceEntity;
    
    private Long serviceId;

    private List<RegistryEntity> deproList;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) {
			serviceEntity = serviceService.findById(serviceId);
			deproList = service.findRegistriesForDepro(serviceEntity.getShortName());

		}
		if (! authBean.isUserServiceAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public void depro(RegistryEntity registry) {
		logger.debug("Deprovsion registry {} (user {})", registry.getId(), registry.getUser().getEppn());
		deproList.remove(registry);
		try {
			registerUserService.deprovision(registry, "user-" + sessionManager.getUserId());
		} catch (RegisterException e) {
			logger.warn("Deprovision failed!", e);
		}
	}
	
	public ServiceEntity getServiceEntity() {
		return serviceEntity;
	}

	public void setServiceEntity(ServiceEntity serviceEntity) {
		this.serviceEntity = serviceEntity;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public List<RegistryEntity> getDeproList() {
		return deproList;
	}

}
