package edu.kit.scc.webreg.service.identity;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.equal;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@ApplicationScoped
public class IdentityUpdater implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private IdentityDao dao;

	public void updateIdentity(UserEntity user) {
		IdentityEntity identity = user.getIdentity();

		if (identity.getPrefUser() == null) {
			logger.info("User (idty-{}) has no preferred identity. Setting actual login user {}", identity.getId(),
					user.getId());
			identity.setPrefUser(user);
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
