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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.transaction.UserTransaction;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;

import edu.kit.scc.webreg.annotations.RetryTransaction;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.ScriptEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.MisconfiguredServiceException;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class KnowledgeSessionServiceImpl implements KnowledgeSessionService {

	@Inject
	private Logger logger;

	@Inject
	private IdentityDao identityDao;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private UserDao userDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private KnowledgeSessionSingleton singleton;

	@Inject
	private UserTransaction userTransaction;

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
	@RetryTransaction
	public List<Object> checkRule(BusinessRulePackageEntity rulePackage, IdentityEntity identity)
			throws MisconfiguredServiceException {
		identity = identityDao.fetch(identity.getId());
		return singleton.checkIdentityRule(rulePackage, identity);
	}

	@Override
	@RetryTransaction
	public List<String> checkScriptAccess(ScriptEntity scriptEntity, IdentityEntity identity) {
		identity = identityDao.fetch(identity.getId());

		return singleton.checkScriptAccess(scriptEntity, identity);
	}

	@Override
	@RetryTransaction
	public List<Object> checkServiceAccessRule(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String executor) throws MisconfiguredServiceException {
		user = userDao.fetch(user.getId());
		service = serviceDao.fetch(service.getId());
		registry = registryDao.fetch(registry.getId());

		return singleton.checkServiceAccessRule(user, service, registry, executor, true);
	}

	@Override
	@RetryTransaction
	public List<Object> checkServiceAccessRule(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String executor, Boolean withCache) throws MisconfiguredServiceException {
		user = userDao.fetch(user.getId());
		service = serviceDao.fetch(service.getId());
		if (registry != null) {
			// registry may be null, in case of user is not registered to service yet
			registry = registryDao.fetch(registry.getId());
		}

		return singleton.checkServiceAccessRule(user, service, registry, executor, withCache);
	}

	@Override
	@RetryTransaction
	public Map<RegistryEntity, List<Object>> checkRules(List<RegistryEntity> registryList, IdentityEntity identity,
			String executor, Boolean withCache) {
		identity = identityDao.fetch(identity.getId());
		List<RegistryEntity> loadedRegistryList = new ArrayList<RegistryEntity>(registryList.size());
		for (RegistryEntity registry : registryList) {
			loadedRegistryList.add(registryDao.fetch(registry.getId(), LockModeType.OPTIMISTIC_FORCE_INCREMENT));
		}
		return singleton.checkRules(loadedRegistryList, identity, executor, withCache);
	}
}
