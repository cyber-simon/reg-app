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

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.util.BBCodeConverter;
import edu.kit.scc.webreg.util.FacesMessageGenerator;

@ManagedBean
@ViewScoped
public class ServiceAdminDetailsBean implements Serializable {

 	private static final long serialVersionUID = 1L;

	@Inject
    private ServiceService serviceService;
    
    @Inject
    private AuthorizationBean authBean;

	@Inject
	private BBCodeConverter bbCodeConverter;

	@Inject
	private FacesMessageGenerator messageGenerator;
	
    private ServiceEntity serviceEntity;
    
    private Long serviceId;

	private String serviceDescBB;
    
	private String descriptionEdit, shortDescriptionEdit;
	
	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) {
			serviceEntity = serviceService.findWithPolicies(serviceId);
			serviceDescBB = bbCodeConverter.convert(serviceEntity.getDescription());
			descriptionEdit = serviceEntity.getDescription();
			shortDescriptionEdit = serviceEntity.getShortDescription();
		}

		if (! authBean.isUserServiceAdmin(serviceEntity))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public void updateDescription() {
		serviceDescBB = bbCodeConverter.convert(descriptionEdit);
	}
	
	public void saveDescription() {
		updateDescription();
		serviceEntity.setDescription(descriptionEdit);
		serviceService.save(serviceEntity);
		messageGenerator.addResolvedInfoMessage("descriptionMsg", "info", "item_saved", true);
	}
	
	public void updateShortDescription() {
		
	}
	
	public void saveShortDescription() {
		updateShortDescription();
		serviceEntity.setShortDescription(shortDescriptionEdit);
		serviceService.save(serviceEntity);
		messageGenerator.addResolvedInfoMessage("shortDescriptionMsg", "info", "item_saved", true);		
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

	public String getServiceDescBB() {
		return serviceDescBB;
	}

	public String getDescriptionEdit() {
		return descriptionEdit;
	}

	public void setDescriptionEdit(String descriptionEdit) {
		this.descriptionEdit = descriptionEdit;
	}

	public String getShortDescriptionEdit() {
		return shortDescriptionEdit;
	}

	public void setShortDescriptionEdit(String shortDescriptionEdit) {
		this.shortDescriptionEdit = shortDescriptionEdit;
	}

}
