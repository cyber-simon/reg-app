package edu.kit.scc.webreg.dto.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AdminUserDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dao.identity.IdentityDao;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.identity.IdentityEntity;

@ApplicationScoped
public class DtoServiceAccessChecker {

	@Inject
	private AdminUserDao adminUserDao;
	
	@Inject 
	private IdentityDao identityDao;

	@Inject 
	private UserDao userDao;
	
	@Inject
	private RoleDao roleDao;

	public Boolean checkAccess(ServiceEntity service, Long userId) {
		Set<RoleEntity> roles = resolveRoles(userId);
		
		if (roles != null && roles.contains(service.getGroupAdminRole())) {
			return true;
		}
		else {
			return false;
		}
	}

	public Boolean checkAccess(GroupEntity group, Long userId) {

		Set<RoleEntity> userRoles = resolveRoles(userId);
		if (userRoles == null) {
			return false;
		}

		Set<RoleEntity> groupAdminRoles = group.getAdminRoles();
		
		groupAdminRoles.retainAll(userRoles);
		if (! groupAdminRoles.isEmpty()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Set<RoleEntity> resolveRoles(Long userId) {
		AdminUserEntity adminUser = adminUserDao.fetch(userId);
		Set<RoleEntity> userRoles = null;
		if (adminUser == null) {
			IdentityEntity identity = identityDao.fetch(userId);
			List<UserEntity> userList = userDao.findByIdentity(identity); 
			userRoles = new HashSet<>();
			
			for (UserEntity user : userList) {
				userRoles.addAll(roleDao.findByUser(user));
			}
		}
		else {
			userRoles = adminUser.getRoles();
		}
		return userRoles;
	}
}
