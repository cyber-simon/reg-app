package edu.kit.scc.webreg.service.identity;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;

import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@ApplicationScoped
public class IdentityUpdater implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	public void updateIdentity(UserEntity user) {
		IdentityEntity identity = user.getIdentity();
		
		if (identity.getPrefUser() == null) {
			logger.info("User (idty-{}) has no preferred identity. Setting actual login user {}", identity.getId(), user.getId());
			identity.setPrefUser(user);
		}
		
		if (identity.getGeneratedLocalUsername() == null) {
			logger.debug("No local generated username for identity {}. Generating one...", user.getIdentity());
			String generatedName = RandomStringUtils.randomAlphabetic(3).toLowerCase() + RandomStringUtils.randomNumeric(4);
			logger.debug("Generated username for identity {}: {}", user.getIdentity(), generatedName);
			user.getIdentity().setGeneratedLocalUsername(generatedName);
		}
	}
}
