package edu.kit.scc.webreg.service;

import java.io.Serializable;
import java.util.List;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;
import edu.kit.scc.webreg.session.SessionManager;

public interface AuthorizationService extends Serializable {

	List<RegistryEntity> loadAll(SessionManager sessionManager, Long identityId);

}
