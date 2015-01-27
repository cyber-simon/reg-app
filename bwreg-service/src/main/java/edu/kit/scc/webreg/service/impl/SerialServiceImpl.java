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
package edu.kit.scc.webreg.service.impl;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.entity.SerialEntity;
import edu.kit.scc.webreg.service.SerialService;

@Stateless
public class SerialServiceImpl extends BaseServiceImpl<SerialEntity, Long> implements SerialService {

	private static final long serialVersionUID = 1L;

	@Inject
	private SerialDao dao;
	
	@Override
	public SerialEntity findByName(String name) {
		return dao.findByName(name);
	}
	
	@Override
	public Long next(String name) {
		SerialEntity serial = dao.findByName(name);
		Long value = serial.getActual();
		value++;
		serial.setActual(value);
		dao.persist(serial);
		return value;
	}
	
	@Override
	public void createIfNotExistant(String name, Long initalValue) {
		try {
			dao.findByName(name);
		} catch (NoResultException nre) {
			SerialEntity serial = dao.createNew();
			serial.setName(name);
			serial.setActual(initalValue);
			dao.persist(serial);
		}
		
	}

	@Override
	protected BaseDao<SerialEntity, Long> getDao() {
		return dao;
	}
}
