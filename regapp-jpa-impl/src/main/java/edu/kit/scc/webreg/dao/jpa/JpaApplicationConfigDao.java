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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.greaterThan;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.ApplicationConfigDao;
import edu.kit.scc.webreg.entity.ApplicationConfigEntity;
import edu.kit.scc.webreg.entity.ApplicationConfigEntity_;

@Named
@ApplicationScoped
public class JpaApplicationConfigDao extends JpaBaseDao<ApplicationConfigEntity> implements ApplicationConfigDao {

	@Override
	public ApplicationConfigEntity findActive() {
		return find(equal(ApplicationConfigEntity_.activeConfig, Boolean.TRUE));
	}

	@Override
	public ApplicationConfigEntity findReloadActive(Date date) {
		return find(and(equal(ApplicationConfigEntity_.activeConfig, Boolean.TRUE),
				greaterThan(ApplicationConfigEntity_.dirtyStamp, date)));
	}

	@Override
	public Class<ApplicationConfigEntity> getEntityClass() {
		return ApplicationConfigEntity.class;
	}

}
