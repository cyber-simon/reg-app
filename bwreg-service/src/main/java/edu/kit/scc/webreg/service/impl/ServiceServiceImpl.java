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
package edu.kit.scc.webreg.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.reg.GroupCapable;
import edu.kit.scc.webreg.service.reg.RegisterUserService;
import edu.kit.scc.webreg.service.reg.SetPasswordCapable;

@Stateless
public class ServiceServiceImpl extends BaseServiceImpl<ServiceEntity, Long> implements ServiceService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private ServiceDao dao;
	
	@Inject
	private RegisterUserService registerUserService;
	
	@Override
	public ServiceEntity updateCapabilities(ServiceEntity service) {
		try {
			service.setPasswordCapable(
					registerUserService.getWorkflowInstance(service.getRegisterBean()) instanceof SetPasswordCapable);
			service.setGroupCapable(
					registerUserService.getWorkflowInstance(service.getRegisterBean()) instanceof GroupCapable);
		} catch (Exception e) {
			logger.warn("Could not set Capabilities on service {}: {}", service.getName(), e.toString());
		}
		return dao.persist(service);
	}
	
	@Override
	public List<ServiceEntity> findAllPublishedWithServiceProps() {
		return dao.findAllPublishedWithServiceProps();
	}	

	@Override
	public ServiceEntity findByShortName(String shortName) {
		return dao.findByShortName(shortName);
	}
	
	@Override
	public List<ServiceEntity> findByAdminRole(RoleEntity role) {
		return dao.findByAdminRole(role);
	}

	@Override
	public List<ServiceEntity> findByParentService(ServiceEntity service) {
		return dao.findByParentService(service);
	}

	@Override
	public List<ServiceEntity> findByHotlineRole(RoleEntity role) {
		return dao.findByHotlineRole(role);
	}

	@Override
	public List<ServiceEntity> findByApproverRole(RoleEntity role) {
		return dao.findByApproverRole(role);
	}

	@Override
	public List<ServiceEntity> findByGroupAdminRole(RoleEntity role) {
		return dao.findByGroupAdminRole(role);
	}

	@Override
	public List<ServiceEntity> findAllWithPolicies() {
		return dao.findAllWithPolicies();
	}

	@Override
	public List<ServiceEntity> findAllByImage(ImageEntity image) {
		return dao.findAllByImage(image);
	}

	@Override
	public ServiceEntity findWithPolicies(Long id) {
		return dao.findWithPolicies(id);
	}
	
	@Override
	public ServiceEntity findByIdWithServiceProps(Long id) {
		return dao.findByIdWithServiceProps(id);
	}

	@Override
	protected BaseDao<ServiceEntity, Long> getDao() {
		return dao;
	}
}
