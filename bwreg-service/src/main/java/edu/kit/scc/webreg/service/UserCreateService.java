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
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.saml.SamlIdentifier;

public interface UserCreateService {

	SamlUserEntity createUser(SamlUserEntity user, Map<String, List<Object>> attributeMap, String executor, StringBuffer debugLog)
			throws UserUpdateException;

	SamlUserEntity preCreateUser(SamlIdpMetadataEntity idpEntity, SamlSpConfigurationEntity spEntity, SamlIdentifier samlIdentifier,
			String locale, Map<String, List<Object>> attributeMap) throws UserUpdateException;

	SamlUserEntity createAndLinkUser(IdentityEntity identity, SamlUserEntity user,
			Map<String, List<Object>> attributeMap, String executor) throws UserUpdateException;

	SamlUserEntity postCreateUser(SamlUserEntity user, Map<String, List<Object>> attributeMap, String executor)
			throws UserUpdateException;

	IdentityEntity preMatchIdentity(SamlUserEntity user, Map<String, List<Object>> attributeMap);

}
