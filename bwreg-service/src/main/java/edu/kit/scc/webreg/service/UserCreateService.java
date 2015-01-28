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
package edu.kit.scc.webreg.service;

import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;

public interface UserCreateService {

	UserEntity createUser(UserEntity user, Map<String, List<Object>> attributeMap, String executor)
			throws RegisterException;

	UserEntity preCreateUser(SamlIdpMetadataEntity idpEntity, SamlSpConfigurationEntity spEntity, String persistentId,
			String locale, Map<String, List<Object>> attributeMap) throws RegisterException;

}
