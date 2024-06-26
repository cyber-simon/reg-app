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
package edu.kit.scc.webreg.service.oauth;

import edu.kit.scc.webreg.dao.jpa.oauth.OAuthRpConfigurationDao;
import edu.kit.scc.webreg.entity.oauth.OAuthRpConfigurationEntity;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class OAuthRpConfigurationService extends BaseServiceImpl<OAuthRpConfigurationEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private OAuthRpConfigurationDao dao;
	
	@Override
	protected OAuthRpConfigurationDao getDao() {
		return dao;
	}
}
