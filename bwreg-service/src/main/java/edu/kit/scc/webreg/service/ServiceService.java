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
package edu.kit.scc.webreg.service;

import java.util.List;

import edu.kit.scc.webreg.entity.ImageEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

public interface ServiceService extends BaseService<ServiceEntity> {

	ServiceEntity findWithPolicies(Long id);

	List<ServiceEntity> findAllByImage(ImageEntity image);

	ServiceEntity findByIdWithServiceProps(Long id);

	ServiceEntity findByShortName(String shortName);

	List<ServiceEntity> findAllPublishedWithServiceProps();

	List<ServiceEntity> findByParentService(ServiceEntity service);

}
