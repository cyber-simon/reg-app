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

import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;

public interface OidcUserCreateService {

	OidcUserEntity createUser(OidcUserEntity user, Map<String, List<Object>> attributeMap, String executor)
			throws UserUpdateException;

	OidcUserEntity preCreateUser(Long rpConfigId,
			String locale, Map<String, List<Object>> attributeMap) throws UserUpdateException;

}
