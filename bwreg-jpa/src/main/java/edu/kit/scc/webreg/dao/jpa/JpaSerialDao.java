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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.entity.SerialEntity;

@Named
@ApplicationScoped
public class JpaSerialDao extends JpaBaseDao<SerialEntity, Long> implements SerialDao {

    @Override
	public SerialEntity findByName(String name) {
		try {
			return (SerialEntity) em.createQuery("select e from SerialEntity e where e.name = :name")
					.setParameter("name", name)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
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
