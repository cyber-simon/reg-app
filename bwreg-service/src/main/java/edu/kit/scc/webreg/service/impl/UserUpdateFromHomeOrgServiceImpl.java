package edu.kit.scc.webreg.service.impl;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.lessThan;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.notEqual;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.or;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.ops.NullOrder;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserEntity_;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.UserUpdateFromHomeOrgService;
import edu.kit.scc.webreg.service.oidc.client.OidcUserUpdater;
import edu.kit.scc.webreg.service.user.UserUpdater;

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

	@Override
	public void updateUserAsync(UserEntity user, String executor) {
		logger.debug("Starting update for user {} ({})", user.getId(), user.getEppn());
		user = userDao.fetch(user.getId());

		try {
			if (user instanceof SamlUserEntity) {
				userUpdater.updateUserFromIdp((SamlUserEntity) user, "update-all-users-from-idp-job");
			} else if (user instanceof OidcUserEntity) {
				oidcUserUpdate.updateUserFromOP((OidcUserEntity) user, "update-all-users-from-idp-job", null);
			} else {
				logger.warn("Don't know how to update user {} from class {}. Rescheduling in 14 days", user.getId(),
						user.getClass().getName());
				user.setScheduledUpdate(new Date(System.currentTimeMillis() + 14L * 24L * 60L * 60L * 1000L));
			}

		} catch (UserUpdateException e) {
			logger.info("Could not update user: {} (cause: {})", e.getMessage(), e.getCause());
		}
	}

	@Override
	public List<UserEntity> findScheduledUsers(Integer limit) {
		Date now = new Date();
		return userDao.findAll(withLimit(limit), SortBy.ascendingBy(UserEntity_.scheduledUpdate, NullOrder.NULL_FIRST),
				and(notEqual(UserEntity_.userStatus, UserStatus.DEREGISTERED),
				or(lessThan(UserEntity_.scheduledUpdate, now), isNull(UserEntity_.scheduledUpdate))));
	}

}
