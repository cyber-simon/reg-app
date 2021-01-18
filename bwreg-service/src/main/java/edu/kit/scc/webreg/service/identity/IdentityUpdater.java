package edu.kit.scc.webreg.service.identity;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@ApplicationScoped
public class IdentityUpdater implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;

	@Inject
	private UserDao userDao;
	
	public void updateIdentity(UserEntity user) {
		user = userDao.merge(user);
		IdentityEntity identity = user.getIdentity();
		
		if (identity.getPrefUser() == null) {
			logger.info("User (idty-{}) has no preferred identity. Setting actual login user {}", identity.getId(), user.getId());
			identity.setPrefUser(user);
		}
	}
}
