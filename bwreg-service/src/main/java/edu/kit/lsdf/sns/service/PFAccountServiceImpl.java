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
package edu.kit.lsdf.sns.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.ServiceService;
import edu.kit.scc.webreg.service.reg.ldap.PropertyReader;

@Stateless
public class PFAccountServiceImpl implements PFAccountService {

	@Inject
	private ServiceService serviceService;

	@Override
	public PFAccount findById(String id, ServiceEntity serviceEntity) throws RegisterException {

		serviceEntity = serviceService.findByIdWithServiceProps(serviceEntity.getId());
		PFWorker pfWorker = new PFWorker(PropertyReader.newRegisterPropReader(serviceEntity), null);
		return pfWorker.getAccountInfoById(id);
	}

	@Override
	public PFAccount findByUsername(String username, ServiceEntity serviceEntity) throws RegisterException {

		serviceEntity = serviceService.findByIdWithServiceProps(serviceEntity.getId());
		PFWorker pfWorker = new PFWorker(PropertyReader.newRegisterPropReader(serviceEntity), null);
		return pfWorker.getAccountInfoByUsername(username);
	}

	@Override
	public PFAccount update(PFAccount account, ServiceEntity serviceEntity) throws RegisterException {

		serviceEntity = serviceService.findByIdWithServiceProps(serviceEntity.getId());
		PFWorker pfWorker = new PFWorker(PropertyReader.newRegisterPropReader(serviceEntity), null);
		return pfWorker.storeAccount(account);
	}
}
