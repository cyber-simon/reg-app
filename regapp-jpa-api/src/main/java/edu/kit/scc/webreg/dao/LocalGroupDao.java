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
package edu.kit.scc.webreg.dao;

import java.util.List;

import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;

public interface LocalGroupDao extends BaseDao<LocalGroupEntity> {

	LocalGroupEntity findByName(String name);

	List<LocalGroupEntity> findAllActiveGroupsByService(PaginateBy paginateBy, ServiceEntity service);

	Number countAllByService(ServiceEntity service);

}
