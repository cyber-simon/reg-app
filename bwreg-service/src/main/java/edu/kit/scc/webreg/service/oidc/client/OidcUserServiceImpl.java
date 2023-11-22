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
package edu.kit.scc.webreg.service.oidc.client;

import java.io.Serializable;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.oidc.OidcUserDao;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;

@Stateless
public class OidcUserServiceImpl extends BaseServiceImpl<OidcUserEntity> implements OidcUserService, Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private OidcUserDao dao;
	
	@Inject
	private OidcUserUpdater userUpdater;
	
	@Override
	public OidcUserEntity updateUserFromOp(OidcUserEntity user, String executor) throws UserUpdateException {
		user = dao.fetch(user.getId());
		return userUpdater.updateUserFromOP(user, executor, null);
	}

	@Override
	protected BaseDao<OidcUserEntity> getDao() {
		return dao;
	}
}
