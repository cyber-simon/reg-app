package edu.kit.scc.webreg.dto.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.AdminUserDao;
import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dao.LocalGroupDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dao.ServiceGroupFlagDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.GroupDetailEntityMapper;
import edu.kit.scc.webreg.dto.mapper.GroupEntityMapper;
import edu.kit.scc.webreg.entity.AdminUserEntity;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.GroupStatus;
import edu.kit.scc.webreg.entity.LocalGroupEntity;
import edu.kit.scc.webreg.entity.RoleEntity;
import edu.kit.scc.webreg.entity.ServiceBasedGroupEntity;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.ServiceGroupFlagEntity;
import edu.kit.scc.webreg.entity.ServiceGroupStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.MultipleGroupEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.GenericRestInterfaceException;
import edu.kit.scc.webreg.exc.NoServiceFoundException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;

@Stateless
public class GroupDtoServiceImpl extends BaseDtoServiceImpl<GroupEntity, GroupEntityDto, Long> implements GroupDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private EventSubmitter eventSubmitter;
	
	@Inject
	private RoleDao roleDao;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private ServiceDao serviceDao;
	
	@Inject
	private AdminUserDao adminUserDao;
	
	@Inject
	private GroupEntityMapper mapper;

	@Inject
	private GroupDetailEntityMapper detailMapper;

	@Inject
	private LocalGroupDao localGroupDao;

	@Inject
	private SerialDao serialDao;
	
	@Inject
	private ServiceGroupFlagDao groupFlagDao;
	
	@Inject
	private GroupDao dao;

	@Override
	public GroupEntityDto findById(Long id, Long userId, Boolean withDetails) throws RestInterfaceException {
		GroupEntity entity = dao.findById(id);
		if (entity == null)
			throw new NoUserFoundException("no such group");
		
		if (! checkAccess(entity, userId))
			throw new UnauthorizedException("Not authorized");
		
		GroupEntityDto dto = createNewDto();
		if (withDetails)
			detailMapper.copyProperties(entity, dto);
		else
			mapper.copyProperties(entity, dto);
		return dto;
	}
	
	@Override
	public GroupEntityDto addUserToGroup(Long groupId, Long userId, Long callerId) throws RestInterfaceException {
		GroupEntity group = dao.findById(groupId);
		if (group == null)
			throw new NoUserFoundException("no such group");
		
		if (! checkAccess(group, callerId))
			throw new UnauthorizedException("Not authorized");
		
		UserEntity user = userDao.findById(userId);
		if (user == null)
			throw new NoUserFoundException("no such user");

		if (dao.findByUser(user).contains(group))
			throw new NoUserFoundException("user already in group");
		
		dao.addUserToGroup(user, group);
		
		if (group instanceof ServiceBasedGroupEntity) {
			List<ServiceGroupFlagEntity> flagList = groupFlagDao.findByGroup((ServiceBasedGroupEntity) group);
			for (ServiceGroupFlagEntity flag : flagList) {
				flag.setStatus(ServiceGroupStatus.DIRTY);
			}
			
			HashSet<GroupEntity> gl = new HashSet<GroupEntity>();
			gl.add(group);
			MultipleGroupEvent mge = new MultipleGroupEvent(gl);
			try {
				eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "user-" + userId);
			} catch (EventSubmitException e) {
				logger.warn("Exeption", e);
			}
		}

		GroupEntityDto dto = createNewDto();
		mapper.copyProperties(group, dto);
		return dto;
	}

	@Override
	public GroupEntityDto removeUserToGroup(Long groupId, Long userId, Long callerId) throws RestInterfaceException {
		GroupEntity group = dao.findById(groupId);
		if (group == null)
			throw new NoUserFoundException("no such group");
		
		if (! checkAccess(group, callerId))
			throw new UnauthorizedException("Not authorized");
		
		UserEntity user = userDao.findById(userId);
		if (user == null)
			throw new NoUserFoundException("no such user");

		if (! dao.findByUser(user).contains(group))
			throw new NoUserFoundException("user is not in group");
		
		dao.removeUserGromGroup(user, group);
		
		if (group instanceof ServiceBasedGroupEntity) {
			List<ServiceGroupFlagEntity> flagList = groupFlagDao.findByGroup((ServiceBasedGroupEntity) group);
			for (ServiceGroupFlagEntity flag : flagList) {
				flag.setStatus(ServiceGroupStatus.DIRTY);
			}
			
			HashSet<GroupEntity> gl = new HashSet<GroupEntity>();
			gl.add(group);
			MultipleGroupEvent mge = new MultipleGroupEvent(gl);
			try {
				eventSubmitter.submit(mge, EventType.GROUP_UPDATE, "user-" + userId);
			} catch (EventSubmitException e) {
				logger.warn("Exeption", e);
			}
		}

		GroupEntityDto dto = createNewDto();
		mapper.copyProperties(group, dto);
		return dto;
	}

	@Override
	public GroupEntityDto create(String ssn, String name, Long userId) throws RestInterfaceException {
		LocalGroupEntity entity = localGroupDao.findByName(name);
		if ((entity != null) && (entity.getGroupStatus().equals(GroupStatus.ACTIVE)))
			throw new GenericRestInterfaceException("group already exists");

		ServiceEntity service = serviceDao.findByShortName(ssn);
		if (service == null) 
			throw new NoServiceFoundException("no such service");
		
		if (! checkAccess(service, userId))
			throw new UnauthorizedException("Not authorized");
		
		entity = localGroupDao.createNew();
		entity.setName(name);
		entity.setAdminRoles(new HashSet<RoleEntity>());
		entity.setParents(new HashSet<GroupEntity>());
		entity.setChildren(new HashSet<GroupEntity>());
		entity.getAdminRoles().add(service.getGroupAdminRole());
		entity.setGidNumber(serialDao.next("gid-number-serial").intValue());
		entity = localGroupDao.persist(entity);

		ServiceGroupFlagEntity groupFlag = groupFlagDao.createNew();
		groupFlag.setService(service);
		groupFlag.setGroup(entity);
		groupFlag.setStatus(ServiceGroupStatus.CLEAN);
		
		groupFlag = groupFlagDao.persist(groupFlag);

		GroupEntityDto dto = createNewDto();
		mapper.copyProperties(entity, dto);
		return dto;
	}
	
	@Override
	public GroupEntityDto findByName(String name, Long userId, Boolean withDetails) throws RestInterfaceException {
		GroupEntity entity = dao.findByName(name);
		if (entity == null)
			throw new NoUserFoundException("no such group");

		if (! checkAccess(entity, userId))
			throw new UnauthorizedException("Not authorized");
		
		GroupEntityDto dto = createNewDto();
		if (withDetails)
			detailMapper.copyProperties(entity, dto);
		else
			mapper.copyProperties(entity, dto);
		return dto;
	}

	protected Boolean checkAccess(ServiceEntity service, Long userId) {
		Set<RoleEntity> roles = resolveRoles(userId);
		
		if (roles != null && roles.contains(service.getGroupAdminRole())) {
			return true;
		}
		else {
			return false;
		}
	}
	
	protected Boolean checkAccess(GroupEntity group, Long userId) {

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
	
	protected Set<RoleEntity> resolveRoles(Long userId) {
		AdminUserEntity adminUser = adminUserDao.findById(userId);
		Set<RoleEntity> userRoles = null;
		if (adminUser == null) {
			UserEntity user = userDao.findById(userId);
			if (user != null) {
				userRoles = new HashSet<>(roleDao.findByUser(user));
			}
		}
		else {
			userRoles = adminUser.getRoles();
		}
		return userRoles;
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
