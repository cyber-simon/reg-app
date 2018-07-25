package edu.kit.scc.webreg.dto.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ExternalUserDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ExternalUserEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ExternalUserReverseEntityMapper;
import edu.kit.scc.webreg.entity.EventType;
import edu.kit.scc.webreg.entity.ExternalUserAdminRoleEntity;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.event.EventSubmitter;
import edu.kit.scc.webreg.event.UserEvent;
import edu.kit.scc.webreg.exc.EventSubmitException;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.exc.UserCreateException;

@Stateless
public class ExternalUserDtoServiceImpl extends BaseDtoServiceImpl<ExternalUserEntity, ExternalUserEntityDto, Long> implements ExternalUserDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private ExternalUserEntityMapper mapper;

	@Inject
	private ExternalUserReverseEntityMapper reverseMapper;
	
	@Inject
	private ExternalUserDao dao;

	@Inject
	private SerialDao serialDao;
	
	@Inject
	private RoleDao roleDao;
	
	@Inject
	private EventSubmitter eventSubmitter;
	
	@Override
	public ExternalUserEntityDto findByExternalId(String externalId) throws NoUserFoundException {
		ExternalUserEntity entity = dao.findByExternalId(externalId);
		if (entity == null)
			throw new NoUserFoundException("no such user");
		ExternalUserEntityDto dto = createNewDto();
		mapper.copyProperties(entity, dto);
		return dto;
	}

	@Override
	public List<ExternalUserEntityDto> findByAttribute(String key, String value, ExternalUserAdminRoleEntity adminRole) throws NoUserFoundException {
		List<ExternalUserEntity> userList = dao.findByAttribute(key, value, adminRole);
		List<ExternalUserEntityDto> dtoList = new ArrayList<>();
		
		for (ExternalUserEntity user : userList) {
			ExternalUserEntityDto dto = createNewDto();
			mapper.copyProperties(user, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}
	
	@Override
	public List<ExternalUserEntityDto> findByGeneric(String key, String value, ExternalUserAdminRoleEntity adminRole) throws NoUserFoundException {
		List<ExternalUserEntity> userList = dao.findByGeneric(key, value, adminRole);
		List<ExternalUserEntityDto> dtoList = new ArrayList<>();
		
		for (ExternalUserEntity user : userList) {
			ExternalUserEntityDto dto = createNewDto();
			mapper.copyProperties(user, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}
	
	@Override
	public void createExternalUser(ExternalUserEntityDto dto, ExternalUserAdminRoleEntity role) throws RestInterfaceException {
		ExternalUserEntity entity = dao.findByExternalId(dto.getExternalId());
		if (entity != null)
			throw new UserCreateException("user already exists");
		
		role = (ExternalUserAdminRoleEntity) roleDao.findById(role.getId());
		
		entity = dao.createNew();
		reverseMapper.copyProperties(dto, entity);
		entity.setUidNumber(serialDao.next("uid-number-serial").intValue());
		entity.setUserStatus(UserStatus.ACTIVE);
		entity.setAdmin(role);
		entity = dao.persist(entity);
	}
	
	@Override
	public void updateExternalUser(ExternalUserEntityDto dto, ExternalUserAdminRoleEntity role) throws RestInterfaceException {
		ExternalUserEntity entity = dao.findByExternalId(dto.getExternalId());
		if (entity == null)
			throw new NoUserFoundException("no such user");
		
		if (role.equals(entity.getAdmin())) {
			reverseMapper.copyProperties(dto, entity);
			fireUserChangeEvent(entity, "external");
		}
		else {
			throw new UnauthorizedException("You are not authorized to modify this external user");
		}
	}

	@Override
	public void activateExternalUser(String externalId, ExternalUserAdminRoleEntity role) throws RestInterfaceException {
		ExternalUserEntity entity = dao.findByExternalId(externalId);
		if (entity == null)
			throw new NoUserFoundException("no such user");

		if (role.equals(entity.getAdmin())) {
			entity.setUserStatus(UserStatus.ACTIVE);
			fireUserChangeEvent(entity, "external");
		}
		else {
			throw new UnauthorizedException("You are not authorized to modify this external user");
		}
	}
		
	@Override
	public void deactivateExternalUser(String externalId, ExternalUserAdminRoleEntity role) throws RestInterfaceException {
		ExternalUserEntity entity = dao.findByExternalId(externalId);
		if (entity == null)
			throw new NoUserFoundException("no such user");

		if (role.equals(entity.getAdmin())) {
			entity.setUserStatus(UserStatus.ON_HOLD);
			fireUserChangeEvent(entity, "external");
		}
		else {
			throw new UnauthorizedException("You are not authorized to modify this external user");
		}
	}
		
	@Override
	protected BaseEntityMapper<ExternalUserEntity, ExternalUserEntityDto, Long> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<ExternalUserEntity, Long> getDao() {
		return dao;
	}


	protected void fireUserChangeEvent(UserEntity user, String executor) {
		
		UserEvent userEvent = new UserEvent(user);
		
		try {
			eventSubmitter.submit(userEvent, EventType.USER_UPDATE, executor);
		} catch (EventSubmitException e) {
			logger.warn("Could not submit event", e);
		}
	}
}
