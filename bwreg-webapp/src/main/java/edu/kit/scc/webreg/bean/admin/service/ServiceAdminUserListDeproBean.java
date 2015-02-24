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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.model.GenericLazyDataModel;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;

@ManagedBean
@ViewScoped
public class ServiceAdminUserListDeproBean implements Serializable {

	private static final long serialVersionUID = 1L;

    @Inject
    private RegistryService service;

    @Inject
    private ServiceService serviceService;

    @Inject
    private AuthorizationBean authBean;
    
    private ServiceEntity serviceEntity;
    
    private Long serviceId;

    private List<RegistryEntity> deproList;
    
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) {
			if (authBean.isUserServiceAdmin(serviceId)) {
				serviceEntity = serviceService.findById(serviceId);
				deproList = service.findRegistriesForDepro(serviceEntity.getShortName());
			}
			else
				throw new NotAuthorizedException("Nicht autorisiert");
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
