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
package edu.kit.scc.webreg.service.identity;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class IdentityServiceImpl extends BaseServiceImpl<IdentityEntity, Long> implements IdentityService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private IdentityDao dao;

	@Inject
	private UserDao userDao;
	
	@Override
	public void createMissingIdentities() {
		logger.info("Creating missing identities...");

		List<UserEntity> userList = userDao.findMissingIdentity();
		for (UserEntity user : userList) {
			logger.info("Creating identity for user {}", user.getId());
			IdentityEntity id = dao.createNew();
			id = dao.persist(id);
			user.setIdentity(id);
		}
		logger.info("Creating missing identities done.");
	}

	@Override
	protected BaseDao<IdentityEntity, Long> getDao() {
		return dao;
	}

}
