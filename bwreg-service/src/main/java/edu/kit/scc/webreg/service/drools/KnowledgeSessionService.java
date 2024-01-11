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
package edu.kit.scc.webreg.service.drools;

import java.util.List;

import org.kie.api.runtime.KieSession;

import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;

public interface KnowledgeSessionService {

	KieSession getStatefulSession(String packageName, String knowledgeBaseName,
			String knowledgeBaseVersion);

	List<Object> checkServiceAccessRule(UserEntity user,
			ServiceEntity service, RegistryEntity registry, String executor)
			throws MisconfiguredServiceException;

	KieSession getStatefulSession(String unitId);

	List<Object> checkServiceAccessRule(UserEntity user,
			ServiceEntity service, RegistryEntity registry, String executor,
			Boolean withCache) throws MisconfiguredServiceException;


	List<Object> checkRule(BusinessRulePackageEntity rulePackage, IdentityEntity identity)
			throws MisconfiguredServiceException;

	List<String> checkScriptAccess(ScriptEntity scriptEntity, IdentityEntity identity);

	RegistryEntity checkRule(RegistryEntity registry, IdentityEntity identity, String executor,
			Boolean withCache);
}
