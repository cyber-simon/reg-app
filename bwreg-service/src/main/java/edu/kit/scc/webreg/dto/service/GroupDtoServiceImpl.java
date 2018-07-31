package edu.kit.scc.webreg.dto.service;

import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.AdminUserDao;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.GroupEntityMapper;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;

@Stateless
public class GroupDtoServiceImpl extends BaseDtoServiceImpl<GroupEntity, GroupEntityDto, Long> implements GroupDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private RoleDao roleDao;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private AdminUserDao adminUserDao;
	
	@Inject
	private GroupEntityMapper mapper;

	@Inject
	private GroupDao dao;

	@Override
	public GroupEntityDto findById(Long id, Long userId) throws RestInterfaceException {
		GroupEntity entity = dao.findById(id);
		if (entity == null)
			throw new NoUserFoundException("no such group");
		
		if (! checkAccess(entity, userId))
			throw new UnauthorizedException("Not authorized");
		
		GroupEntityDto dto = createNewDto();
		mapper.copyProperties(entity, dto);
		return dto;
	}
	
	@Override
	public GroupEntityDto findByName(String name, Long userId) throws RestInterfaceException {
		GroupEntity entity = dao.findByName(name);
		if (entity == null)
			throw new NoUserFoundException("no such group");

		if (! checkAccess(entity, userId))
			throw new UnauthorizedException("Not authorized");
		
		GroupEntityDto dto = createNewDto();
		mapper.copyProperties(entity, dto);
		return dto;
	}
	
	protected Boolean checkAccess(GroupEntity group, Long userId) {
		AdminUserEntity adminUser = adminUserDao.findById(userId);
		Set<RoleEntity> userRoles;
		if (adminUser == null) {
			UserEntity user = userDao.findById(userId);
			if (user == null) {
				return false;
			}
			else {
				userRoles = new HashSet<>(roleDao.findByUser(user));
			}
		}
		else {
			userRoles = adminUser.getRoles();
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
	
	@Override
	protected BaseEntityMapper<GroupEntity, GroupEntityDto, Long> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<GroupEntity, Long> getDao() {
		return dao;
	}

}
