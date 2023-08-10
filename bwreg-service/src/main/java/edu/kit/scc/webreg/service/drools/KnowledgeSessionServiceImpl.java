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
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;
import edu.kit.scc.webreg.entity.SamlSpConfigurationEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;

@Stateless
public class KnowledgeSessionServiceImpl implements KnowledgeSessionService {

	@Inject
	private IdentityDao identityDao;

	@Inject
	private KnowledgeSessionSingleton singleton;

	@Override
	public KieSession getStatefulSession(String packageName, String knowledgeBaseName, String knowledgeBaseVersion) {

		KieServices ks = KieServices.Factory.get();
		ReleaseId releaseId = ks.newReleaseId(packageName, knowledgeBaseName, knowledgeBaseVersion);

		return getStatefulSession(ks, releaseId);
	}

	@Override
	public KieSession getStatefulSession(String unitId) {
		String[] splits = unitId.split(":");

		if (splits.length != 3)
			throw new IllegalArgumentException("unitId must contain two :");

		return getStatefulSession(splits[0], splits[1], splits[2]);
	}

	protected KieSession getStatefulSession(KieServices ks, ReleaseId releaseId) {
		KieContainer kc = ks.newKieContainer(releaseId);
		return kc.newKieSession();
	}

	@Override
	public List<Object> checkRule(BusinessRulePackageEntity rulePackage, IdentityEntity identity)
			throws MisconfiguredServiceException {
		identity = identityDao.merge(identity);

		return singleton.checkIdentityRule(rulePackage, identity);

	}

	@Override
	public List<String> checkScriptAccess(ScriptEntity scriptEntity, IdentityEntity identity) {
		identity = identityDao.merge(identity);

		return singleton.checkScriptAccess(scriptEntity, identity);
	}

	@Override
	public List<Object> checkRule(String unitId, UserEntity user, Map<String, List<Object>> attributeMap,
			Assertion assertion, SamlIdpMetadataEntity idp, EntityDescriptor idpEntityDescriptor,
			SamlSpConfigurationEntity sp) throws MisconfiguredServiceException {

		return singleton.checkSamlLoginRule(unitId, user, attributeMap, assertion, idp, idpEntityDescriptor, sp);
	}

	@Override
	public List<Object> checkServiceAccessRule(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String executor) throws MisconfiguredServiceException {
		return singleton.checkServiceAccessRule(user, service, registry, executor, true);
	}

	@Override
	public List<Object> checkServiceAccessRule(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String executor, Boolean withCache) throws MisconfiguredServiceException {
		return singleton.checkServiceAccessRule(user, service, registry, executor, withCache);
	}

	@Override
	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, IdentityEntity identity,
			String executor) {
		return singleton.checkRules(registryList, identity, executor);
	}

	@Override
	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, IdentityEntity identity,
			String executor, Boolean withCache) {

		return singleton.checkRules(registryList, identity, executor, withCache);
	}
}
