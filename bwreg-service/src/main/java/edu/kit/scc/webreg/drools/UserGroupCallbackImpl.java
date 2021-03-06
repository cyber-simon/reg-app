package edu.kit.scc.webreg.drools;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.api.task.UserGroupCallback;

import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.service.RoleService;
import edu.kit.scc.webreg.service.UserService;

@ApplicationScoped
public class UserGroupCallbackImpl implements UserGroupCallback {

	@Inject
	private UserService userService;
	
	@Inject
	private RoleService roleService;
	
	@Override
	public boolean existsUser(String userId) {
		List<UserEntity> user = userService.findByEppn(userId);
		if (user.size() == 0) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public boolean existsGroup(String groupId) {
		RoleEntity role = roleService.findByName(groupId);
		if (role == null) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public List<String> getGroupsForUser(String userId, List<String> groupIds,
			List<String> allExistingGroupIds) {
		List<UserEntity> userList = userService.findByEppn(userId);
		if (userList.size() == 0) {
			return null;
		}
		List<String> returnList = new ArrayList<String>();

		for (UserEntity user : userList) {
			List<RoleEntity> roleEntityList = roleService.findByUser(user);
			
			for (RoleEntity role : roleEntityList) {
				returnList.add(role.getName());
			}
		}
		
		return returnList;
	}

}
