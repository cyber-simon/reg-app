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
package edu.kit.scc.webreg.dao.jpa.as;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.as.ASUserAttrDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity;
import edu.kit.scc.webreg.entity.as.ASUserAttrEntity_;

@Named
@ApplicationScoped
public class JpaASUserAttrDao extends JpaBaseDao<ASUserAttrEntity> implements ASUserAttrDao {

	@Override
	public List<ASUserAttrEntity> findForUser(UserEntity user) {
		return findAll(equal(ASUserAttrEntity_.user, user));
	}

	@Override
	public Class<ASUserAttrEntity> getEntityClass() {
		return ASUserAttrEntity.class;
	}

}
