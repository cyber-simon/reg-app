package edu.kit.scc.webreg.service.identity;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

import java.io.Serializable;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@ApplicationScoped
public class IdentityCreater implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private IdentityDao dao;

	public IdentityEntity preCreateIdentity() {
		/**
		 * TODO: Apply mapping rules at this point. At the moment every account gets one
		 * identity. Should possibly be mapped to existing identity in some cases.
		 */
		IdentityEntity identity = dao.createNew();
		identity = dao.persist(identity);

		identity.setTwoFaUserId("idty-" + identity.getId());
		identity.setTwoFaUserName(UUID.randomUUID().toString());

		return identity;
	}

	public void postCreateIdentity(IdentityEntity identity, UserEntity user) {
		if (identity.getPrefUser() == null) {
			identity.setPrefUser(user);
		}

		if (identity.getUidNumber() == null) {
			identity.setUidNumber(user.getUidNumber());
		}

		if (identity.getGeneratedLocalUsername() == null) {
			logger.debug("No local generated username for identity {}. Generating one...", user.getIdentity());
			String generatedName = randomAlphabetic(3).toLowerCase() + randomNumeric(4);
			logger.debug("Generated username for identity {}: {}", user.getIdentity(), generatedName);
			while (dao.find(equal("generatedLocalUsername", generatedName)) != null) {
				generatedName = randomAlphabetic(3).toLowerCase() + randomNumeric(4);
				logger.debug("Generated username is already taken. Try again for identity {}: {}", user.getIdentity(),
						generatedName);
			}
			user.getIdentity().setGeneratedLocalUsername(generatedName);
		}
	}
}
