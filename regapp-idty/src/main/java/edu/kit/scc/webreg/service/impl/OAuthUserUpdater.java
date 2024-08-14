package edu.kit.scc.webreg.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;

import edu.kit.scc.webreg.audit.Auditor;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.attribute.IncomingOAuthAttributeEntity;
import edu.kit.scc.webreg.entity.audit.AuditStatus;
import edu.kit.scc.webreg.entity.oauth.OAuthUserEntity;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.hook.HookManager;
import edu.kit.scc.webreg.hook.UserServiceHook;
import edu.kit.scc.webreg.service.attribute.IncomingAttributesHandler;
import edu.kit.scc.webreg.service.attribute.IncomingOAuthAttributesHandler;
import edu.kit.scc.webreg.service.group.HomeOrgGroupUpdater;
import edu.kit.scc.webreg.service.group.OAuthGroupUpdater;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OAuthUserUpdater extends AbstractUserUpdater<OAuthUserEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private SerialDao serialDao;

	@Inject
	private HookManager hookManager;

	@Inject
	private OAuthGroupUpdater oauthGroupUpdater;

	@Inject
	private IncomingOAuthAttributesHandler incomingAttributeHandler;
	
	public OAuthUserEntity updateUserFromOP(OAuthUserEntity user, String executor, StringBuffer debugLog)
			throws UserUpdateException {
		return updateUserFromHomeOrg(user, null, executor, debugLog);
	}

	public OAuthUserEntity updateUserFromHomeOrg(OAuthUserEntity user, ServiceEntity service, String executor,
			StringBuffer debugLog) throws UserUpdateException {
		throw new UserUpdateException("Not implemented");
	}
	
	public boolean updateUserNew(OAuthUserEntity user, Map<String, List<Object>> attributeMap, String executor,
			Auditor auditor, StringBuffer debugLog, String lastLoginHost) throws UserUpdateException {
		boolean changed = false;

		changed |= preUpdateUser(user, attributeMap, user.getOauthIssuer().getGenericStore(), executor, null, debugLog);
		changed |= updateUserFromAttribute(user, attributeMap, auditor);
		changed |= postUpdateUser(user, attributeMap, user.getOauthIssuer().getGenericStore(), executor, null, debugLog,
				lastLoginHost);

		return changed;
	}

	public boolean updateUserFromAttribute(OAuthUserEntity user, Map<String, List<Object>> attributeMap,
			boolean withoutUidNumber, Auditor auditor) throws UserUpdateException {

		boolean changed = false;

		UserServiceHook completeOverrideHook = null;
		Set<UserServiceHook> activeHooks = new HashSet<UserServiceHook>();

		for (UserServiceHook hook : hookManager.getUserHooks()) {
			if (hook.isResponsible(user, attributeMap)) {

				hook.preUpdateUserFromAttribute(user, attributeMap, auditor);
				activeHooks.add(hook);

				if (hook.isCompleteOverride()) {
					completeOverrideHook = hook;
				}
			}
		}

		if (completeOverrideHook == null) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> userMap = (HashMap<String, Object>) attributeMap.get("user").get(0);

			if (userMap.get("email") != null && (userMap.get("email") instanceof String))
				changed |= compareAndChangeProperty(user, "email", (String) userMap.get("email"), auditor);
			else
				changed |= compareAndChangeProperty(user, "email", null, auditor);

			if (userMap.get("name") != null && (userMap.get("name") instanceof String))
				changed |= compareAndChangeProperty(user, "name", (String) userMap.get("name"), auditor);
			else
				changed |= compareAndChangeProperty(user, "name", null, auditor);

			if ((!withoutUidNumber) && (user.getUidNumber() == null)) {
				user.setUidNumber(serialDao.nextUidNumber().intValue());
				logger.info("Setting UID Number {} for user {}", user.getUidNumber(), user.getEppn());
				auditor.logAction(user.getEppn(), "SET FIELD", "uidNumber", "" + user.getUidNumber(),
						AuditStatus.SUCCESS);
				changed = true;
			}
		} else {
			logger.info("Overriding standard User Update Mechanism! Activator: {}",
					completeOverrideHook.getClass().getName());
		}

		for (UserServiceHook hook : activeHooks) {
			hook.postUpdateUserFromAttribute(user, attributeMap, auditor);
		}

		return changed;
	}

	private boolean compareAndChangeProperty(UserEntity user, String property, String value, Auditor auditor) {
		String s = null;
		String action = null;

		try {
			Object actualValue = PropertyUtils.getProperty(user, property);

			if (actualValue != null && actualValue.equals(value)) {
				// Value didn't change, do nothing
				return false;
			}

			if (actualValue == null && value == null) {
				// Value stayed null
				return false;
			}

			if (actualValue == null) {
				s = "null";
				action = "SET FIELD";
			} else {
				s = actualValue.toString();
				action = "UPDATE FIELD";
			}

			s = s + " -> " + value;
			if (s.length() > 1017)
				s = s.substring(0, 1017) + "...";

			PropertyUtils.setProperty(user, property, value);

			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.SUCCESS);
		} catch (IllegalAccessException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (InvocationTargetException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		} catch (NoSuchMethodException e) {
			logger.warn("This probably shouldn't happen: ", e);
			auditor.logAction(user.getEppn(), action, property, s, AuditStatus.FAIL);
		}

		return true;
	}

	protected void updateFail(OAuthUserEntity user) {
		user.setLastFailedUpdate(new Date());
		user.setScheduledUpdate(getNextScheduledUpdate());
	}

	@Override
	public OAuthUserEntity expireUser(OAuthUserEntity user) throws UserUpdateException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HomeOrgGroupUpdater<OAuthUserEntity> getGroupUpdater() {
		return oauthGroupUpdater;
	}

	@Override
	public Map<String, String> resolveHomeOrgGenericStore(OAuthUserEntity user) {
		return user.getOauthIssuer().getGenericStore();
	}

	@Override
	public IncomingAttributesHandler<IncomingOAuthAttributeEntity> resolveIncomingAttributeHandler(OAuthUserEntity user) {
		return incomingAttributeHandler;
	}
}
