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
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.service.identity.IdentityScriptingEnv;
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

	public void sendUserExpiryWarning(UserEntity user, String emailTemplateName) {
		logger.debug("Sending expiry warning to user {} to e-mail address {}", user.getId(), user.getIdentity().getPrimaryEmail());
		
		Map<String, Object> context = new HashMap<String, Object>(2);
		context.put("user", user);
		context.put("identity", user.getIdentity());

		mailService.sendMail(emailTemplateName, context, true);
		user.setExpireWarningSent(new Date());

	}

}
