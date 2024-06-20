package edu.kit.scc.webreg.service.identity;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import edu.kit.scc.regapp.mail.api.TemplateMailService;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.identity.IdentityEmailAddressDao;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.IdentityEmailAddressEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.inject.Inject;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class IdentityEmailAddressService extends BaseServiceImpl<IdentityEmailAddressEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private IdentityEmailAddressDao dao;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private TemplateMailService mailService;

	@Inject
	private EventSubmitter eventSubmitter;

	public IdentityEmailAddressEntity addEmailAddress(IdentityEntity identity, String emailAddress, String executor) throws AddressException {
		InternetAddress email = new InternetAddress(emailAddress, true);

		IdentityEmailAddressEntity entity = dao.createNew();
		entity.setIdentity(identity);
		entity.setEmailAddress(email.getAddress());
		entity.setVerificationToken(generateToken());
		entity.setTokenValidUntil(generateTokenValidity());
		entity = dao.persist(entity);

		sendVerificationEmail(entity);

		IdentityEmailAddressEvent event = new IdentityEmailAddressEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.EMAIL_ADDRESS_ADDED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}

		return entity;
	}

	public void redoVerification(IdentityEmailAddressEntity entity, String executor) {
		entity = dao.fetch(entity.getId());

		entity.setVerificationToken(generateToken());
		entity.setTokenValidUntil(generateTokenValidity());

		sendVerificationEmail(entity);

		IdentityEmailAddressEvent event = new IdentityEmailAddressEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.EMAIL_ADDRESS_REDO_VERIFICATION, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}

	public boolean checkVerification(IdentityEmailAddressEntity entity, String token, String executor) {
		entity = dao.fetch(entity.getId());
		if (entity.getVerificationToken().equals(token)) {
			entity.setVerificationToken(null);
			entity.setVerifiedOn(new Date());
			entity.setValidUntil(generateValidity());

			IdentityEmailAddressEvent event = new IdentityEmailAddressEvent(entity);
			try {
				eventSubmitter.submit(event, EventType.EMAIL_ADDRESS_VERIFIED, executor);
			} catch (EventSubmitException e) {
				logger.warn("Could not submit event", e);
			}
			return true;
		} else {
			return false;
		}
	}

	private String generateToken() {
		SecureRandom random = new SecureRandom();
		String t = new BigInteger(130, random).toString(32);
		return t;
	}

	private Date generateTokenValidity() {
		Long validity = 30 * 60 * 60 * 1000L;
		if (appConfig.getConfigValue("email_token_validity") != null) {
			validity = Long.parseLong(appConfig.getConfigValue("email_token_validity"));
		}

		return new Date(System.currentTimeMillis() + validity);
	}

	private Date generateValidity() {
		Long validity = 180 * 24 * 60 * 60 * 1000L;
		if (appConfig.getConfigValue("email_validity") != null) {
			validity = Long.parseLong(appConfig.getConfigValue("email_validity"));
		}

		return new Date(System.currentTimeMillis() + validity);
	}

	public void sendVerificationEmail(IdentityEmailAddressEntity emailAddress) {

		logger.debug("Sending Email verification mail for identity {} (email: {})", emailAddress.getIdentity().getId(),
				emailAddress.getEmailAddress());

		String templateName = appConfig.getConfigValueOrDefault("email_verification_template", "email_verification");

		Map<String, Object> context = new HashMap<String, Object>(3);
		context.put("emailAddress", emailAddress);
		context.put("identity", emailAddress.getIdentity());

		mailService.sendMail(templateName, context, true);
		emailAddress.setVerificationSent(new Date());
	}

	@Override
	protected BaseDao<IdentityEmailAddressEntity> getDao() {
		return dao;
	}
}
