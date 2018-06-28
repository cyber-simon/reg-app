package edu.kit.scc.webreg.service;

import edu.kit.scc.webreg.entity.SamlUserEntity;

public interface UserDeleteService {

	void deleteUserData(SamlUserEntity user, String executor);

}
