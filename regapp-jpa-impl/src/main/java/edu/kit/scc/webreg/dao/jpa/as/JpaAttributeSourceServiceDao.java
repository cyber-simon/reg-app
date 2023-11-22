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

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import edu.kit.scc.webreg.dao.as.AttributeSourceServiceDao;
import edu.kit.scc.webreg.dao.jpa.JpaBaseDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity;
import edu.kit.scc.webreg.entity.as.AttributeSourceServiceEntity_;

@Named
@ApplicationScoped
public class JpaAttributeSourceServiceDao extends JpaBaseDao<AttributeSourceServiceEntity>
		implements AttributeSourceServiceDao {

	@Override
	public Class<AttributeSourceServiceEntity> getEntityClass() {
		return AttributeSourceServiceEntity.class;
	}

	@Override
	public AttributeSourceServiceEntity connectService(AttributeSourceEntity as, ServiceEntity service) {
		AttributeSourceServiceEntity entity = createNew();
		entity.setAttributeSource(as);
		entity.setService(service);
		return persist(entity);
	}

	@Override
	public void disconnectService(AttributeSourceEntity as, ServiceEntity service) {
		AttributeSourceServiceEntity entity = find(and(equal(AttributeSourceServiceEntity_.attributeSource, as),
				equal(AttributeSourceServiceEntity_.service, service)));
		delete(entity);
	}

}
