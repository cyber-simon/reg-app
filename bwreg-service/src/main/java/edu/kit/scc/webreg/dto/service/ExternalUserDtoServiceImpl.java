package edu.kit.scc.webreg.dto.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ExternalUserDao;
import edu.kit.scc.webreg.dao.RoleDao;
import edu.kit.scc.webreg.dao.SerialDao;
import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ExternalUserEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ExternalUserReverseEntityMapper;
import edu.kit.scc.webreg.entity.ExternalUserAdminRoleEntity;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.entity.UserStatus;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;
import edu.kit.scc.webreg.exc.UnauthorizedException;
import edu.kit.scc.webreg.exc.UserCreateException;

@Stateless
public class ExternalUserDtoServiceImpl extends BaseDtoServiceImpl<ExternalUserEntity, ExternalUserEntityDto, Long> implements ExternalUserDtoService {

	private static final long serialVersionUID = 1L;

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

}
