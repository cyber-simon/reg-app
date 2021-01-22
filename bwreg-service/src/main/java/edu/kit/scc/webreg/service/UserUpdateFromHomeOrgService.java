package edu.kit.scc.webreg.service;

import java.util.List;

import edu.kit.scc.webreg.entity.UserEntity;

public interface UserUpdateFromHomeOrgService {

	void updateUserAsync(UserEntity user, String executor);

	List<UserEntity> findScheduledUsers(Integer limit);

}
