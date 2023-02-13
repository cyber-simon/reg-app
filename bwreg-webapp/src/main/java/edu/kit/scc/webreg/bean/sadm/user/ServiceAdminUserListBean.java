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
package edu.kit.scc.webreg.bean.sadm.user;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.in;

import java.io.Serializable;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.NotAuthorizedException;
import edu.kit.scc.webreg.model.GenericLazyDataModel;
import edu.kit.scc.webreg.model.GenericLazyDataModelImpl;
import edu.kit.scc.webreg.sec.AuthorizationBean;
import edu.kit.scc.webreg.service.RegistryService;
import edu.kit.scc.webreg.service.ServiceService;

@Named
@ViewScoped
public class ServiceAdminUserListBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private GenericLazyDataModel<RegistryEntity, RegistryService> list;
	private GenericLazyDataModel<RegistryEntity, RegistryService> allList;
	private GenericLazyDataModel<RegistryEntity, RegistryService> otherList;
	private GenericLazyDataModel<RegistryEntity, RegistryService> deletedList;
	private GenericLazyDataModel<RegistryEntity, RegistryService> lostAccessList;

	@Inject
	private RegistryService service;

	@Inject
	private ServiceService serviceService;

	@Inject
	private AuthorizationBean authBean;

	private ServiceEntity serviceEntity;

	private Long serviceId;

	public void preRenderView(ComponentSystemEvent ev) {
		if (serviceEntity == null) {
			serviceEntity = serviceService.fetch(serviceId);
		}

		if (!(authBean.isUserServiceAdmin(serviceEntity) || authBean.isUserServiceHotline(serviceEntity)))
			throw new NotAuthorizedException("Nicht autorisiert");
	}

	public RegistryStatus[] getRegistryStatusList() {
		return RegistryStatus.values();
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

	public GenericLazyDataModel<RegistryEntity, RegistryService> getList() {
		if (list == null) {
			list = new GenericLazyDataModelImpl<>(service,
					and(equal("service", serviceEntity), equal("registryStatus", RegistryStatus.ACTIVE)));
		}
		return list;
	}

	public GenericLazyDataModel<RegistryEntity, RegistryService> getAllList() {
		if (allList == null) {
			allList = new GenericLazyDataModelImpl<>(service, equal("service", serviceEntity));
		}
		return allList;
	}

	public GenericLazyDataModel<RegistryEntity, RegistryService> getOtherList() {
		if (otherList == null) {
			otherList = new GenericLazyDataModelImpl<>(service,
					and(equal("service", serviceEntity),
							in("registryStatus", RegistryStatus.INVALID, RegistryStatus.PENDING, RegistryStatus.CREATED,
									RegistryStatus.BLOCKED, RegistryStatus.ON_HOLD)));
		}
		return otherList;
	}

	public GenericLazyDataModel<RegistryEntity, RegistryService> getDeletedList() {
		if (deletedList == null) {
			deletedList = new GenericLazyDataModelImpl<>(service,
					and(equal("service", serviceEntity), equal("registryStatus", RegistryStatus.DELETED)));
		}
		return deletedList;
	}

	public GenericLazyDataModel<RegistryEntity, RegistryService> getLostAccessList() {
		if (lostAccessList == null) {
			lostAccessList = new GenericLazyDataModelImpl<>(service,
					and(equal("service", serviceEntity), equal("registryStatus", RegistryStatus.LOST_ACCESS)));
		}
		return lostAccessList;
	}
}
