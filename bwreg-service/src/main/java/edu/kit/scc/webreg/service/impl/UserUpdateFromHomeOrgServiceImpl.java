package edu.kit.scc.webreg.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.drools.impl.KnowledgeSessionSingleton;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserUpdateFromHomeOrgService;
import edu.kit.scc.webreg.service.oidc.client.OidcUserUpdater;

@Stateless
public class UserUpdateFromHomeOrgServiceImpl implements UserUpdateFromHomeOrgService, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private UserUpdater userUpdater;

	@Inject
	private OidcUserUpdater oidcUserUpdate;
	
	@Inject
	private KnowledgeSessionSingleton knowledgeSessionService;
	
	@Inject
	private RegistryDao registryDao;
	
	@Inject
	private ServiceDao serviceDao;
	
	@Inject
	private ApplicationConfig appConfig;

	@Override
	public void updateUserAsync(UserEntity user, String executor) {
		logger.debug("Starting update for user {} ({})", user.getId(), user.getEppn());
		user = userDao.merge(user);
		
		try {
			if (user instanceof SamlUserEntity) {
				userUpdater.updateUserFromIdp((SamlUserEntity) user, "update-all-users-from-idp-job");
			}
			else if (user instanceof OidcUserEntity) {
				oidcUserUpdate.updateUserFromOP((OidcUserEntity) user, "update-all-users-from-idp-job", null);
			}
			else {
				logger.warn("Don't know how to update user {} from class {}. Rescheduling in 14 days", user.getId(), user.getClass().getName());
				user.setScheduledUpdate(new Date(System.currentTimeMillis() + 14L * 24L * 60L * 60L * 1000L));
			}
			
		} catch (UserUpdateException e) {
			logger.info("Could not update user: {} (cause: {})", e.getMessage(), e.getCause());
		}
	}

	@Override
	public List<UserEntity> findScheduledUsers(Integer limit) {
		return userDao.findScheduledUsers(limit);
	}
}
