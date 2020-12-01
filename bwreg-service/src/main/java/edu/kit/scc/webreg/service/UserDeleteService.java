package edu.kit.scc.webreg.service;

import edu.kit.scc.webreg.entity.identity.IdentityEntity;

public interface UserDeleteService {

	void deleteUserData(IdentityEntity identity, String executor);

}
