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
package edu.kit.scc.webreg.drools;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kie.api.runtime.KieSession;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;

public interface KnowledgeSessionService {

	KieSession getStatefulSession(String packageName, String knowledgeBaseName,
			String knowledgeBaseVersion);

	List<Object> checkRule(String packageName, String knowledgeBaseName,
			String knowledgeBaseVersion, UserEntity user,
			ServiceEntity service, RegistryEntity registry, String executor)
			throws MisconfiguredServiceException;

	KieSession getStatefulSession(String unitId);

	Map<RegistryEntity, List<Object>> checkRules(
			List<RegistryEntity> registryList, UserEntity user, String executor);

	List<Object> checkRule(String packageName, String knowledgeBaseName,
			String knowledgeBaseVersion, UserEntity user,
			ServiceEntity service, RegistryEntity registry, String executor,
			Boolean withCache) throws MisconfiguredServiceException;

	Map<RegistryEntity, List<Object>> checkRules(
			List<RegistryEntity> registryList, UserEntity user,
			String executor, Boolean withCache);

	List<Object> checkRule(String unitId, UserEntity user,
			Map<String, List<Object>> attributeMap, Assertion assertion,
			SamlIdpMetadataEntity idp, EntityDescriptor idpEntityDescriptor, SamlSpConfigurationEntity sp)
			throws MisconfiguredServiceException;

	List<ServiceEntity> checkServiceFilterRule(String unitId, UserEntity user,
			List<ServiceEntity> serviceList, Set<GroupEntity> groups,
			Set<RoleEntity> roles) throws MisconfiguredServiceException;

}
