package edu.kit.scc.webreg.service.identity;

import static edu.kit.scc.webreg.dao.ops.PaginateBy.withLimit;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.greaterThan;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.isNull;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.lessThan;
import static java.time.temporal.ChronoUnit.DAYS;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.identity.IdentityEmailAddressDao;
import edu.kit.scc.webreg.entity.identity.EmailAddressStatus;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity_;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.exc.VerificationException;
import edu.kit.scc.webreg.service.impl.BaseServiceImpl;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.mail.internet.AddressException;

@Stateless
public class IdentityEmailAddressService extends BaseServiceImpl<IdentityEmailAddressEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private IdentityEmailAddressDao dao;

	@Inject
	private IdentityEmailAddressHandler handler;

	public IdentityEmailAddressEntity addEmailAddress(IdentityEntity identity, String emailAddress, String executor)
			throws AddressException {
		return handler.addEmailAddress(identity, emailAddress, executor);
	}

	public void redoVerification(IdentityEmailAddressEntity entity, String executor) {
		entity = dao.fetch(entity.getId());
		handler.redoVerification(entity, executor);
	}

	public void deleteEmailAddress(IdentityEmailAddressEntity entity, String executor) {
		entity = dao.fetch(entity.getId());
		handler.deleteEmailAddress(entity, executor);
	}

	public void setPrimaryEmailAddress(IdentityEmailAddressEntity entity, String executor) {
		entity = dao.fetch(entity.getId());
		handler.setPrimaryEmailAddress(entity, executor);
	}

	public IdentityEmailAddressEntity checkVerification(IdentityEntity identity, String token, String executor)
			throws VerificationException {
		return handler.checkVerification(identity, token, executor);
	}

	public void expireEmailAddress(IdentityEmailAddressEntity entity, String executor) {
		entity = dao.fetch(entity.getId());
		handler.setPrimaryEmailAddress(entity, executor);
	}

	public void sendExpiryEmailAddress(IdentityEmailAddressEntity entity, String executor) {
		entity = dao.fetch(entity.getId());
		handler.sendExpiryWarningEmail(entity, executor);
	}

	public List<IdentityEmailAddressEntity> findEmailToExpiryWarning(int limit, int days) {
		Date dateInNDays = Date.from(Instant.now().plus(days, DAYS));
		return dao.findAll(withLimit(limit),
				and(equal(IdentityEmailAddressEntity_.emailStatus, EmailAddressStatus.VERIFIED),
						isNull(IdentityEmailAddressEntity_.expireWarningSent),
						greaterThan(IdentityEmailAddressEntity_.validUntil, new Date()),
						lessThan(IdentityEmailAddressEntity_.validUntil, dateInNDays)));
	}

	public List<IdentityEmailAddressEntity> findVerifiedToExpire(Integer limit) {
		return dao.findAll(withLimit(limit),
				and(equal(IdentityEmailAddressEntity_.emailStatus, EmailAddressStatus.VERIFIED),
						lessThan(IdentityEmailAddressEntity_.validUntil, new Date())));
	}

	@Override
	protected BaseDao<IdentityEmailAddressEntity> getDao() {
		return dao;
	}
}
