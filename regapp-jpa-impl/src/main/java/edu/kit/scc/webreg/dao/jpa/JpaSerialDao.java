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

import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.entity.SerialEntity;
import edu.kit.scc.webreg.entity.SerialEntity_;

@Named
@ApplicationScoped
public class JpaSerialDao extends JpaBaseDao<SerialEntity> implements SerialDao {

	@Override
	public SerialEntity findByName(String name) {
		return find(equal(SerialEntity_.name, name));
	}

	@Override
	public Long next(String name) {
		SerialEntity serial = findByName(name);
		Long value = serial.getActual();
		value++;
		serial.setActual(value);
		return value;
	}

	@Override
	public Class<SerialEntity> getEntityClass() {
		return SerialEntity.class;
	}
}
