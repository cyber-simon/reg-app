package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ejb.Asynchronous;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.MDC;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.drools.OverrideAccess;
import edu.kit.scc.webreg.drools.UnauthorizedUser;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.ServiceRegisterEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.LoginFailedException;
import edu.kit.scc.webreg.exc.NoRegistryFoundException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UserNotUniqueException;
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
	private SamlUserUpdater userUpdater;

	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;

	@Inject
	private RegistryDao registryDao;

	@Inject
	private ServiceDao serviceDao;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private EventSubmitter eventSubmitter;

	@Override
	public Map<String, String> updateUser(Integer uidNumber, String serviceShortName, String localHostName,
			String executor) throws RestInterfaceException {

		UserEntity user = findUser(uidNumber);
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
	public Map<String, String> updateUser(String eppn, String serviceShortName, String localHostName, String executor)
			throws RestInterfaceException {

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
			throws RestInterfaceException {

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
		map.put("last_update", df.format(user.getLastUpdate()));

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
			logger.info("Skipping async user update for {} with id {}", new Object[] { user.getEppn(), user.getId() });
		} else if ((user.getLastFailedUpdate() != null)
				&& (System.currentTimeMillis() - user.getLastFailedUpdate().getTime()) < expireTime) {
			logger.info("Skipping async user update for {} with id {} (last failed)",
					new Object[] { user.getEppn(), user.getId() });
		} else {
			logger.info("Performing async update for {} with id {}", new Object[] { user.getEppn(), user.getId() });

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
	public Map<String, String> updateUserByGenericStore(String key, String value, String serviceShortName,
			String localHostName, String executor) throws RestInterfaceException {

		List<UserEntity> userList = userDao.findByGeneric(key, value);

		if (userList.size() > 1) {
			throw new UserNotUniqueException("Found more than one user for key,value");
		} else if (userList.size() == 0) {
			throw new NoUserFoundException("No user found for key,value");
		}

		UserEntity user = userList.get(0);

		ServiceEntity service = findService(serviceShortName);
		if (service == null)
			throw new NoServiceFoundException("no such service");

		RegistryEntity registry = findRegistry(user, service);
		if (registry == null)
			throw new NoRegistryFoundException("user not registered for service");

		return update(user, service, registry, localHostName, executor);
	}

	@Override
	public Map<String, String> updateUserByAttributeStore(String key, String value, String serviceShortName,
			String localHostName, String executor) throws RestInterfaceException {

		List<UserEntity> userList = userDao.findByAttribute(key, value);

		if (userList.size() > 1) {
			throw new UserNotUniqueException("Found more than one user for key,value");
		} else if (userList.size() == 0) {
			throw new NoUserFoundException("No user found for key,value");
		}

		UserEntity user = userList.get(0);

		ServiceEntity service = findService(serviceShortName);
		if (service == null)
			throw new NoServiceFoundException("no such service");

		RegistryEntity registry = findRegistry(user, service);
		if (registry == null)
			throw new NoRegistryFoundException("user not registered for service");

		return update(user, service, registry, localHostName, executor);
	}

	@Override
	public Map<String, String> updateUser(Long regId, String localHostName, String executor)
			throws RestInterfaceException {
		RegistryEntity registry = registryDao.fetch(regId);

		if (registry == null) {
			logger.info("No registry found for id {}", regId);
			throw new NoRegistryFoundException("registry unknown");
		}

		return update(registry.getUser(), registry.getService(), registry, localHostName, executor);
	}

	private Map<String, String> update(UserEntity user, ServiceEntity service, RegistryEntity registry,
			String localHostName, String executor) throws RestInterfaceException {

		MDC.put("userId", "" + user.getId());

		// Default expiry Time after which an attrq is issued to IDP in millis
		Long expireTime = 10L * 1000L;
		if (service.getServiceProps() != null && service.getServiceProps().containsKey("attrq_expire_time")) {
			expireTime = Long.parseLong(service.getServiceProps().get("attrq_expire_time"));
		}

		// Time for last user update. If null, last user update is not checked
		Long lastUserUpdate = null;
		if (service.getServiceProps() != null && service.getServiceProps().containsKey("attrq_last_user_update")) {
			lastUserUpdate = Long.parseLong(service.getServiceProps().get("attrq_last_user_update"));
		}

		String checkMethod = "standard";
		if (service.getServiceProps() != null && service.getServiceProps().containsKey("attrq_check_method")) {
			checkMethod = service.getServiceProps().get("attrq_check_method");
		}

		if (checkMethod.equalsIgnoreCase("no_attrq")) {
			/*
			 * Don't perform any user update per attribute query or refresh token, if this
			 * is set
			 */

			logger.info("Performing no user update for {} with id {}, as configuration with service",
					new Object[] { user.getEppn(), user.getId() });
		} else if (checkMethod.equalsIgnoreCase("attrq_optional")) {
			/*
			 * Perform user update per attribute query or refresh token, but proceed if it
			 * failed
			 */
			try {
				if ((user.getLastUpdate() != null)
						&& ((System.currentTimeMillis() - user.getLastUpdate().getTime()) < expireTime)) {
					logger.info("Skipping user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });
				} else {
					logger.info("Performing user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });

					// TODO check for OIDC user entity (refresh token?)
					if (user instanceof SamlUserEntity)
						user = userUpdater.updateUserFromHomeOrg((SamlUserEntity) user, service, executor, null);
				}
			} catch (UserUpdateException e) {
				logger.warn("Could not update user (attrq is optional, continue with login process) {}: {}",
						e.getMessage(), user.getEppn());
			}
		} else {
			/*
			 * This is the standard case, where attribute query or refresh token is
			 * mandatory
			 */
			try {
				if ((user.getLastUpdate() != null)
						&& ((System.currentTimeMillis() - user.getLastUpdate().getTime()) < expireTime)) {
					logger.info("Skipping user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });
				} else {
					logger.info("Performing user update for {} with id {}",
							new Object[] { user.getEppn(), user.getId() });

					// TODO check for OIDC user entity (refresh token?)
					if (user instanceof SamlUserEntity)
						user = userUpdater.updateUserFromHomeOrg((SamlUserEntity) user, service, executor, null);
				}
			} catch (UserUpdateException e) {
				logger.warn("Could not update user {}: {}", e.getMessage(), user.getEppn());
				throw new UserUpdateFailedException("user update failed: " + e.getMessage());
			}
		}

		if (registry == null)
			throw new NoRegistryFoundException("No such registry");

		if ((user.getLastUpdate() == null) || ((lastUserUpdate != null)
				&& ((System.currentTimeMillis() - user.getLastUpdate().getTime()) > lastUserUpdate))) {
			logger.info("Last user update is due for {} with id {}. Setting to LOST_ACCESS.",
					new Object[] { user.getEppn(), user.getId() });

			registry.setRegistryStatus(RegistryStatus.LOST_ACCESS);
			registry.setStatusMessage("update-due");
			registry.setLastStatusChange(new Date());

			ServiceRegisterEvent registerEvent = new ServiceRegisterEvent(registry);

			try {
				eventSubmitter.submit(registerEvent, EventType.USER_LOST_ACCESS, executor);
			} catch (EventSubmitException e) {
				logger.warn("Could not submit event", e);
			}

			throw new LoginFailedException("last user update is too long ago\n" + user.getLastUpdate());
		}

		List<Object> objectList = checkRules(user, service, registry);

		StringBuilder sb = new StringBuilder();
		for (Object o : objectList) {
			if (o instanceof OverrideAccess) {
				objectList.clear();
				sb.setLength(0);
				logger.debug("Removing requirements due to OverrideAccess");
				break;
			} else if (o instanceof UnauthorizedUser) {
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
		map.put("uidNumber", "" + user.getUidNumber());
		map.put("last_update", df.format(user.getLastUpdate()));

		return map;
	}

	private List<Object> checkRules(UserEntity user, ServiceEntity service, RegistryEntity registry) {
		return knowledgeSessionService.checkServiceAccessRule(user, service, registry, "user-self", false);
	}

	private RegistryEntity findRegistry(UserEntity user, ServiceEntity service) {
		RegistryEntity registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.ACTIVE);

		if (registry == null) {
			/*
			 * Also check for Lost_access registries. They should also be allowed to be
			 * rechecked.
			 */
			registry = registryDao.findByServiceAndUserAndStatus(service, user, RegistryStatus.LOST_ACCESS);
		}

		if (registry == null) {
			/*
			 * Also check for On_hold registries. They should also be allowed to be
			 * rechecked.
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
		List<UserEntity> userList = userDao.findByEppn(eppn);
		if (userList.size() == 0)
			return null;
		else if (userList.size() > 1)
			return null;

		UserEntity user = userList.get(0);
		user = userDao.findByIdWithStore(user.getId());

		return user;
	}

	private UserEntity findUser(Integer uidNumber) {
		UserEntity user = userDao.findByUidNumber(uidNumber);

		if (user != null) {
			user = userDao.findByIdWithStore(user.getId());
		}

		return user;
	}

}
