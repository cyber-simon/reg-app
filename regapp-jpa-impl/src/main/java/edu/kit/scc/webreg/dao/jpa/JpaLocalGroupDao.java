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
package edu.kit.scc.webreg.dao.jpa;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.LocalGroupDao;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.LocalGroupEntity_;

@Named
@ApplicationScoped
public class JpaLocalGroupDao extends JpaBaseDao<LocalGroupEntity> implements LocalGroupDao {

	@Override
	public LocalGroupEntity findByName(String name) {
		return find(equal(LocalGroupEntity_.name, name));
	}

	@Override
	public Class<LocalGroupEntity> getEntityClass() {
		return LocalGroupEntity.class;
	}

}
