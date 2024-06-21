package edu.kit.scc.webreg.service.identity;

import java.io.Serializable;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.identity.IdentityEmailAddressDao;
import edu.kit.scc.webreg.entity.identity.IdentityEmailAddressEntity;
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

	@Override
	protected BaseDao<IdentityEmailAddressEntity> getDao() {
		return dao;
	}
}
