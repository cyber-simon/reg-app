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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.TextPropertyDao;
import edu.kit.scc.webreg.entity.TextPropertyEntity;
import edu.kit.scc.webreg.entity.TextPropertyEntity_;

@Named
@ApplicationScoped
public class JpaTextPropertyDao extends JpaBaseDao<TextPropertyEntity> implements TextPropertyDao {

	@Override
	public TextPropertyEntity findByKeyAndLang(String key, String language) {
		return find(and(equal(TextPropertyEntity_.key, key), equal(TextPropertyEntity_.language, language)));
	}

	@Override
	public Class<TextPropertyEntity> getEntityClass() {
		return TextPropertyEntity.class;
	}

}
