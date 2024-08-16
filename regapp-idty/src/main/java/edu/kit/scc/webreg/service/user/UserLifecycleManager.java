package edu.kit.scc.webreg.service.user;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import edu.kit.scc.regapp.mail.impl.TemplateMailSender;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.audit.AuditDetailDao;
import edu.kit.scc.webreg.dao.audit.AuditEntryDao;
import edu.kit.scc.webreg.entity.SamlUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.oauth.OAuthUserEntity;
import edu.kit.scc.webreg.entity.oidc.OidcUserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.exc.UserUpdateException;
import edu.kit.scc.webreg.service.identity.IdentityScriptingEnv;
import edu.kit.scc.webreg.service.impl.OAuthUserUpdater;
import edu.kit.scc.webreg.service.impl.OidcUserUpdater;
import edu.kit.scc.webreg.service.impl.SamlUserUpdater;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserLifecycleManager implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private AuditEntryDao auditDao;

	@Inject
	private AuditDetailDao auditDetailDao;

	@Inject
	private UserDao userDao;

	@Inject
	private IdentityScriptingEnv scriptingEnv;

	@Inject
	private TemplateMailSender mailService;

	@Inject
	private EventSubmitter eventSubmitter;

	@Inject
	private SamlUserUpdater userUpdater;

	@Inject
	private OidcUserUpdater oidcUserUpdater;

	@Inject
	private OAuthUserUpdater oauthUserUpdater;

	public void sendUserExpiryWarning(UserEntity user, String emailTemplateName) {
		logger.debug("Trying to send expiry warning to user {} to e-mail address {}. First updating...", user.getId(),
				user.getIdentity().getPrimaryEmail());

		try {
			if (user instanceof SamlUserEntity) {
				user = userUpdater.updateUserFromHomeOrg((SamlUserEntity) user, null, "user-expire-job", null);
			} else if (user instanceof OidcUserEntity) {
				user = oidcUserUpdater.updateUserFromHomeOrg((OidcUserEntity) user, null, "user-expire-job", null);
			} else if (user instanceof OAuthUserEntity) {
				user = oauthUserUpdater.updateUserFromHomeOrg((OAuthUserEntity) user, null, "user-expire-job", null);
			}
			
			logger.info("Update didn't fail. Don't send expiry warning to user");
		} catch (UserUpdateException e) {
			logger.debug("Update failed, sending expiry warning to user {} to e-mail address {}", user.getId(),
					user.getIdentity().getPrimaryEmail());
			sendMail(user, emailTemplateName);
			user.setExpireWarningSent(new Date());
		}
	}

	public void expireUser(UserEntity user, String emailTemplateName) {
		logger.debug("Trying to expire user {} with e-mail address {}", user.getId(),
				user.getIdentity().getPrimaryEmail());

		if (user instanceof SamlUserEntity) {
			user = userUpdater.expireUser((SamlUserEntity) user, "user-expire-job");
		} else if (user instanceof OidcUserEntity) {
			user = oidcUserUpdater.expireUser((OidcUserEntity) user, "user-expire-job");
		} else if (user instanceof OAuthUserEntity) {
			user = oauthUserUpdater.expireUser((OAuthUserEntity) user, "user-expire-job");
		}

		sendMail(user, emailTemplateName);
		user.setExpiredSent(new Date());
	}

	private void sendMail(UserEntity user, String emailTemplateName) {
		Map<String, Object> context = new HashMap<String, Object>(2);
		context.put("user", user);
		context.put("identity", user.getIdentity());
		mailService.sendMail(emailTemplateName, context, true);
	}
}
