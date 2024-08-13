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
import edu.kit.scc.webreg.service.impl.UserUpdater;
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
	private UserUpdater userUpdater;

	@Inject
	private OidcUserUpdater oidcUserUpdater;

	@Inject
	private OAuthUserUpdater oauthUserUpdater;

	public void sendUserExpiryWarning(UserEntity user, String emailTemplateName) {
		logger.debug("Sending expiry warning to user {} to e-mail address {}", user.getId(),
				user.getIdentity().getPrimaryEmail());
		sendMail(user, emailTemplateName);
		user.setExpireWarningSent(new Date());

	}

	public void expireUser(UserEntity user, String emailTemplateName) {
		logger.debug("Trying to expire user {} with e-mail address {}", user.getId(),
				user.getIdentity().getPrimaryEmail());

		try {
			if (user instanceof SamlUserEntity) {
				user = userUpdater.updateUserFromIdp((SamlUserEntity) user, "user-expire-job");
				// TODO: call and implement expire function
			}
			else if (user instanceof OidcUserEntity) {
				user = oidcUserUpdater.updateUserFromOP((OidcUserEntity) user, "user-expire-job", null);
				// TODO: call and implement expire function
			}
			else if (user instanceof OAuthUserEntity) {
				user = oidcUserUpdater.updateUserFromOP((OidcUserEntity) user, "user-expire-job", null);
				// TODO: call and implement expire function
			}
		} catch (UserUpdateException e) {

		}
		//sendMail(user, emailTemplateName);
		//user.setExpiredSent(new Date());
	}

	private void sendMail(UserEntity user, String emailTemplateName) {
		Map<String, Object> context = new HashMap<String, Object>(2);
		context.put("user", user);
		context.put("identity", user.getIdentity());
		mailService.sendMail(emailTemplateName, context, true);
	}
}
