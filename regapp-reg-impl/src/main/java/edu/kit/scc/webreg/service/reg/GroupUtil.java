package edu.kit.scc.webreg.service.reg;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;

@ApplicationScoped
public class GroupUtil {

	@Inject
	private UserDao userDao;
	
	public Set<UserEntity> rollUsersForGroup(GroupEntity group, ServiceEntity service) {
		Set<UserEntity> users = new HashSet<UserEntity>();
		
		rollUsersForGroupIntern(group, service, users, 0, 3);
		
		return users;
	}
	
	private void rollUsersForGroupIntern(GroupEntity group, ServiceEntity service, Set<UserEntity> users, int depth, int maxDepth) {
		users.addAll(userDao.findByGroupAndService(group, service, RegistryStatus.ACTIVE));
		
		if (depth <= maxDepth) {
			for (GroupEntity child : group.getChildren()) {
				rollUsersForGroupIntern(child, service, users, depth + 1, maxDepth);
			}
		}
	}

	public Set<UserEntity> rollUsersForGroup(GroupEntity group) {
		Set<UserEntity> users = new HashSet<UserEntity>();
		
		rollUsersForGroupIntern(group, users, 0, 3);
		
		return users;
	}
	
	private void rollUsersForGroupIntern(GroupEntity group, Set<UserEntity> users, int depth, int maxDepth) {
		users.addAll(userDao.findByGroup(group));
		
		if (depth <= maxDepth) {
			for (GroupEntity child : group.getChildren()) {
				rollUsersForGroupIntern(child, users, depth + 1, maxDepth);
			}
		}
	}
}
