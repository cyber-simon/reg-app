package edu.kit.scc.webreg.service.identity;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;

import edu.kit.scc.regapp.mail.impl.TemplateMailSender;
import edu.kit.scc.webreg.bootstrap.ApplicationConfig;
import edu.kit.scc.webreg.dao.identity.IdentityEmailAddressDao;
import edu.kit.scc.webreg.dao.ops.RqlExpressions;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.identity.EmailAddressStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.IdentityEmailAddressEvent;
import edu.kit.scc.webreg.event.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.VerificationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

@ApplicationScoped
public class IdentityEmailAddressHandler implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private IdentityEmailAddressDao dao;

	@Inject
	private ApplicationConfig appConfig;

	@Inject
	private TemplateMailSender mailService;

	@Inject
	private EventSubmitter eventSubmitter;

	public void setPrimaryEmailAddress(IdentityEmailAddressEntity entity, String executor) {
		entity.getIdentity().setPrimaryEmail(entity);
	}
	
	public IdentityEmailAddressEntity addEmailAddressFromAttribute(IdentityEntity identity, String emailAddress, String executor)
			throws AddressException {
		InternetAddress email = new InternetAddress(emailAddress, true);

		IdentityEmailAddressEntity entity = dao.createNew();
		entity.setIdentity(identity);
		entity.setEmailAddress(email.getAddress());
		entity.setEmailStatus(EmailAddressStatus.FROM_ATTRIBUTE_UNVERIFIED);
		entity = dao.persist(entity);

		return entity;
		
	}
	
	public IdentityEmailAddressEntity addEmailAddress(IdentityEntity identity, String emailAddress, String executor)
			throws AddressException {
		InternetAddress email = new InternetAddress(emailAddress, true);

		IdentityEmailAddressEntity entity = dao.createNew();
		entity.setIdentity(identity);
		entity.setEmailAddress(email.getAddress());
		entity.setVerificationToken(generateToken());
		entity.setTokenValidUntil(generateTokenValidity());
		entity.setEmailStatus(EmailAddressStatus.UNVERIFIED);
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

	public void deleteEmailAddress(IdentityEmailAddressEntity entity, String executor) {
		if (entity.equals(entity.getIdentity().getPrimaryEmail())) {
			entity.getIdentity().setPrimaryEmail(null);
		}
		dao.delete(entity);
	}

	public IdentityEmailAddressEntity checkVerification(IdentityEntity identity, String token, String executor)
			throws VerificationException {
		IdentityEmailAddressEntity entity = dao
				.find(RqlExpressions.equal(IdentityEmailAddressEntity_.verificationToken, token));

		if (entity == null) {
			throw new VerificationException("no_token_found");
		}
		
		if (!identity.equals(entity.getIdentity())) {
			throw new VerificationException("not_owner");
		}

		if (entity.getTokenValidUntil().before(new Date())) {
			throw new VerificationException("token_expired");
		}
		
		entity.setVerificationToken(null);
		entity.setVerifiedOn(new Date());
		entity.setValidUntil(generateValidity());
		entity.setEmailStatus(EmailAddressStatus.VERIFIED);

		if (identity.getPrimaryEmail() == null) {
			// use identity from jpa session, the object from method call is detached
			identity = entity.getIdentity();
			identity.setPrimaryEmail(entity);
		}
		
		IdentityEmailAddressEvent event = new IdentityEmailAddressEvent(entity);
		try {
			eventSubmitter.submit(event, EventType.EMAIL_ADDRESS_VERIFIED, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
		return entity;
	}

	private String generateToken() {
		SecureRandom random = new SecureRandom();
		String t = new BigInteger(130, random).toString(32);
		return t;
	}

	private Date generateTokenValidity() {
		Long validity = 30 * 60 * 1000L;
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

	protected void sendVerificationEmail(IdentityEmailAddressEntity emailAddress) {

		logger.debug("Sending Email verification mail for identity {} (email: {})", emailAddress.getIdentity().getId(),
				emailAddress.getEmailAddress());

		String templateName = appConfig.getConfigValueOrDefault("email_verification_template", "email_verification");

		Map<String, Object> context = new HashMap<String, Object>(3);
		context.put("emailAddress", emailAddress);
		context.put("identity", emailAddress.getIdentity());

		mailService.sendMail(templateName, context, true);
		emailAddress.setVerificationSent(new Date());
	}
}
