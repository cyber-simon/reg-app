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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceEntity_;
import edu.kit.scc.webreg.service.ServiceService;

@Stateless
public class ServiceServiceImpl extends BaseServiceImpl<ServiceEntity> implements ServiceService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ServiceDao dao;

	@Override
	public List<ServiceEntity> findAllPublishedWithServiceProps() {
		return dao.findAllPublishedWithServiceProps();
	}

	@Override
	public ServiceEntity findByShortName(String shortName) {
		return dao.findByShortName(shortName);
	}

	@Override
	public List<ServiceEntity> findByParentService(ServiceEntity service) {
		return dao.findByParentService(service);
	}

	@Override
	public List<ServiceEntity> findAllByImage(ImageEntity image) {
		return dao.findAll(equal(ServiceEntity_.image, image));
	}

	@Override
	public ServiceEntity findWithPolicies(Long id) {
		return dao.find(equal(ServiceEntity_.id, id), ServiceEntity_.policies);
	}

	@Override
	public ServiceEntity findByIdWithServiceProps(Long id) {
		return dao.findByIdWithServiceProps(id);
	}

	@Override
	protected BaseDao<ServiceEntity> getDao() {
		return dao;
	}
}
