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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.ScriptDao;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ScriptEntity_;

@Named
@ApplicationScoped
public class JpaScriptlDao extends JpaBaseDao<ScriptEntity> implements ScriptDao {

	@Override
	public ScriptEntity findByName(String name) {
		return find(equal(ScriptEntity_.name, name));
	}

	@Override
	public Class<ScriptEntity> getEntityClass() {
		return ScriptEntity.class;
	}

}
