package edu.kit.scc.webreg.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.MDC;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.drools.KnowledgeSessionService;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.entity.BusinessRulePackageEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.LoginFailedException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.exc.UserUpdateFailedException;
import edu.kit.scc.webreg.service.UserUpdateService;

@Stateless
public class UserUpdateServiceImpl implements UserUpdateService, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private UserUpdater userUpdater;

	@Inject
	private KnowledgeSessionService knowledgeSessionService;
	
	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private ServiceDao serviceDao;
	
	@Inject
	private ApplicationConfig appConfig;
	
	@Override
	public Map<String, String> updateUser(String eppn,
			String serviceShortName, String localHostName, String executor)
			throws IOException, RestInterfaceException {

		UserEntity user = findUser(eppn);
		if (user == null)
			throw new NoUserFoundException("no such user");
		
		ServiceEntity service = findService(serviceShortName);
		if (service == null)
			throw new NoServiceFoundException("no such service");
		
		RegistryEntity registry = findRegistry(user, service);
		if (registry == null)
			throw new NoRegistryFoundException("user not registered for service");
		
		return update(user, service, registry, localHostName, executor);
	}

	@Override
	public Map<String, String> updateUser(String eppn, String localHostName, String executor)
			throws IOException, RestInterfaceException {

		UserEntity user = findUser(eppn);
		if (user == null)
			throw new NoUserFoundException("no such user");
		
		if (user instanceof SamlUserEntity) {
			try {
				user = userUpdater.updateUserFromIdp((SamlUserEntity) user, executor);
			} catch (UserUpdateException e) {
				logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
				throw new UserUpdateFailedException("user update failed: " + e.getMessage());
			}		
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Map<String, String> map = new HashMap<String, String>();
		map.put("eppn", user.getEppn());
		map.put("email", user.getEmail());
		map.put("last_update",  df.format(user.getLastUpdate()));
		
		return map;
	}

	@Override
	@Asynchronous
	public void updateUserAsync(String eppn, String localHostName, String executor) {

		UserEntity user = findUser(eppn);
		if (user == null) {
			logger.info("Not updating user. No such user: {}", eppn);
			return;
		}
		
		Long expireTime = 24 * 60 * 60 * 1000L; // one stay standard expire time for async checks
		if (appConfig.getConfigValue("async_userupdate_expire_time") != null) {
			expireTime = Long.parseLong(appConfig.getConfigValue("async_userupdate_expire_time"));
		}
		
		if (((System.currentTimeMillis() - user.getLastUpdate().getTime()) < expireTime)) {
			logger.info("Skipping async user update for {} with id {}", new Object[] {user.getEppn(), user.getId()});
		}
		else if ((user.getLastFailedUpdate() != null) &&
				(System.currentTimeMillis() - user.getLastFailedUpdate().getTime()) < expireTime) {
			logger.info("Skipping async user update for {} with id {} (last failed)", new Object[] {user.getEppn(), user.getId()});
		}
		else {
			logger.info("Performing async update for {} with id {}", new Object[] {user.getEppn(), user.getId()}); 

			if (user instanceof SamlUserEntity) {
				try {
					user = userUpdater.updateUserFromIdp((SamlUserEntity) user, executor);
				} catch (UserUpdateException e) {
					logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
				}
			}
		}
	}

	@Override
	public Map<String, String> updateUser(Long regId, String localHostName, String executor)
			throws IOException, RestInterfaceException {
		RegistryEntity registry = registryDao.findById(regId);

		if (registry == null) {
			logger.info("No registry found for id {}", regId);
			throw new NoRegistryFoundException("registry unknown");
		}
		
		return update(registry.getUser(), registry.getService(), registry, localHostName, executor);
	}

	private Map<String, String> update(UserEntity user, ServiceEntity service, RegistryEntity registry, String localHostName, String executor)
			throws RestInterfaceException {

		MDC.put("userId", "" + user.getId());
		
		// Default expiry Time after which an attrq is issued to IDP in millis
		Long expireTime = 10000L;
		
		if (service.getServiceProps() != null && service.getServiceProps().containsKey("attrq_expire_time")) {
			expireTime = Long.parseLong(service.getServiceProps().get("attrq_expire_time"));
		}
		
		try {
			if ((System.currentTimeMillis() - user.getLastUpdate().getTime()) < expireTime) {
				logger.info("Skipping user update for {} with id {}", new Object[] {user.getEppn(), user.getId()});
			}
			else {
				logger.info("Performing user update for {} with id {}", new Object[] {user.getEppn(), user.getId()}); 
	
				if (user instanceof SamlUserEntity)
					user = userUpdater.updateUserFromIdp((SamlUserEntity) user, service, executor);
			}
		} catch (UserUpdateException e) {
			logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
			throw new UserUpdateFailedException("user update failed: " + e.getMessage());
		}		
		
		if (registry == null)
			throw new NoRegistryFoundException("No such registry");
		
		List<Object> objectList = checkRules(user, service, registry);
		
		StringBuilder sb = new StringBuilder();
		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				objectList.clear();
				sb.setLength(0);
				logger.debug("Removing requirements due to OverrideAccess");
				break;
			}
			else if (o instanceof UnauthorizedUser) {
				String s = ((UnauthorizedUser) o).getMessage();
				sb.append(s);
				sb.append("\n");
			}
		}

		if (sb.length() > 0) {
			throw new LoginFailedException("user not allowd for service\n" + sb.toString());
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Map<String, String> map = new HashMap<String, String>();
		map.put("eppn", user.getEppn());
		map.put("email", user.getEmail());
		map.put("last_update",  df.format(user.getLastUpdate()));
		
		return map;
	}
	
	private List<Object> checkRules(UserEntity user, ServiceEntity service, RegistryEntity registry) {
		List<Object> objectList;
		
		if (service.getAccessRule() == null) {
			objectList = knowledgeSessionService.checkRule("default", "permitAllRule", "1.0.0", user, service, registry, "user-self", false);
		}
		else {
			BusinessRulePackageEntity rulePackage = service.getAccessRule().getRulePackage();
			if (rulePackage != null) {
				objectList = knowledgeSessionService.checkRule(rulePackage.getPackageName(), rulePackage.getKnowledgeBaseName(), 
					rulePackage.getKnowledgeBaseVersion(), user, service, registry, "user-self", false);
			}
			else {
				throw new IllegalStateException("checkServiceAccess called with a rule (" +
							service.getAccessRule().getName() + ") that has no rulePackage");
			}
		}

		return objectList;
	}
	
	private RegistryEntity findRegistry(UserEntity user, ServiceEntity service) {
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);
		
		if (registry == null) {
			/*
			 * Also check for Lost_access registries. They should also be allowed to be rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.LOST_ACCESS);
		}

		if (registry == null) {
			/*
			 * Also check for On_hold registries. They should also be allowed to be rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ON_HOLD);
		}
		
		return registry;
	}

	private ServiceEntity findService(String serviceShortName) {
		ServiceEntity service = serviceDao.findByShortName(serviceShortName);
		
		if (service != null) {
			service = serviceDao.findByIdWithServiceProps(service.getId());
		}
		
		return service;
	}

	private UserEntity findUser(String eppn) {
		UserEntity user = userDao.findByEppn(eppn);

		if (user != null) {
			user = userDao.findByIdWithStore(user.getId());
		}

		return user;
	}

}
