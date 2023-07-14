/*
 * *****************************************************************************
 * Copyright (c) 2014 Michael Simon.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3.0 which accompanies
 * this distribution, and is available at http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Michael Simon - initial
 * *****************************************************************************
 */
package edu.kit.scc.webreg.dao.jpa.project;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import edu.kit.scc.webreg.dao.project.AttributeSourceProjectDao;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity;
import edu.kit.scc.webreg.entity.project.AttributeSourceProjectEntity_;
import edu.kit.scc.webreg.entity.project.ExternalProjectEntity_;

@Named
@ApplicationScoped
public class JpaAttributeSourceProjectDao extends JpaExternalProjectDao<AttributeSourceProjectEntity>
		implements AttributeSourceProjectDao {

	@Override
	public AttributeSourceProjectEntity findByExternalNameAttributeSource(String externalName,
			AttributeSourceEntity attributeSource) {
		return find(and(equal(ExternalProjectEntity_.externalName, externalName),
				equal(AttributeSourceProjectEntity_.attributeSource, attributeSource)));
	}

	@Override
	public Class<AttributeSourceProjectEntity> getEntityClass() {
		return AttributeSourceProjectEntity.class;
	}

}
