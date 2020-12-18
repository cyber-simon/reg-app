package edu.kit.scc.webreg.service;

import java.io.Serializable;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.session.SessionManager;

public interface AuthorizationService extends Serializable {

	List<RegistryEntity> loadAll(SessionManager sessionManager, Long identityId, HttpServletRequest request);

}
