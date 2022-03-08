package edu.kit.scc.webreg.dto.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ExternalUserDao;
import edu.kit.scc.webreg.dao.RegistryDao;
import edu.kit.scc.webreg.dao.ServiceDao;
import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.RegistryEntityMapper;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.service.reg.RegisterUserService;

@Stateless
public class RegistryDtoServiceImpl extends BaseDtoServiceImpl<RegistryEntity, RegistryEntityDto> implements RegistryDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private RegistryEntityMapper mapper;
	
	@Inject
	private RegistryDao dao;
	
	@Inject
	private ExternalUserDao externalUserDao;
	
	@Inject
	private ServiceDao serviceDao;
	
	@Inject
	private RegisterUserService registerUserService;

	@Override
	public List<RegistryEntityDto> findRegistriesByStatus(ServiceEntity service, RegistryStatus status) {
		List<RegistryEntity> regList = dao.findByServiceAndStatus(service, status);
		List<RegistryEntityDto> dtoList = new ArrayList<RegistryEntityDto>(regList.size());
		for (RegistryEntity reg : regList) {
			RegistryEntityDto dto = createNewDto();
			mapper.copyProperties(reg, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}
	
	@Override
	public List<RegistryEntityDto> findRegistriesByAttribute(String key, String value, ServiceEntity service) {
		List<RegistryEntity> regList = dao.findByServiceAndAttribute(key, value, service);
		List<RegistryEntityDto> dtoList = new ArrayList<RegistryEntityDto>(regList.size());
		for (RegistryEntity reg : regList) {
			RegistryEntityDto dto = createNewDto();
			mapper.copyProperties(reg, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}
	
	@Override
	public List<RegistryEntityDto> findRegistriesForDepro(String serviceShortName) {
		List<RegistryEntity> regList = dao.findRegistriesForDepro(serviceShortName);
		List<RegistryEntityDto> dtoList = new ArrayList<RegistryEntityDto>(regList.size());
		for (RegistryEntity reg : regList) {
			RegistryEntityDto dto = createNewDto();
			mapper.copyProperties(reg, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}
	
	@Override
	public List<RegistryEntityDto> findByExternalId(String externalId) {
		ExternalUserEntity user = externalUserDao.findByExternalId(externalId);
		List<RegistryEntity> regList = dao.findByUser(user);
		List<RegistryEntityDto> dtoList = new ArrayList<RegistryEntityDto>(regList.size());
		for (RegistryEntity reg : regList) {
			RegistryEntityDto dto = createNewDto();
			mapper.copyProperties(reg, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}

	@Override
	public List<RegistryEntityDto> findAllExternalBySsn(String ssn) {
		List<RegistryEntity> regList = dao.findAllExternalBySsn(ssn);
		List<RegistryEntityDto> dtoList = new ArrayList<RegistryEntityDto>(regList.size());
		for (RegistryEntity reg : regList) {
			RegistryEntityDto dto = createNewDto();
			mapper.copyProperties(reg, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}

	@Override
	public RegistryEntityDto register(String externalId, String ssn)
			throws RegisterException {
		UserEntity user = externalUserDao.findByExternalId(externalId);
		if (user == null) {
			throw new RegisterException("no such user");
		}
		ServiceEntity service = serviceDao.findByShortName(ssn);
		if (service == null) {
			throw new RegisterException("no such service");
		}
		
		List<RegistryEntity> registryList = dao.findByServiceAndIdentityAndNotStatus(service, user.getIdentity(), 
				RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
		if (registryList.size() > 0) {
			throw new RegisterException("user already registered for service");
		}

		RegistryEntity registry = registerUserService.registerUser(user, service, "external", true, null);
		RegistryEntityDto dto = createNewDto();
		mapper.copyProperties(registry, dto);
		return dto;
	}

	@Override
	public void deregister(String externalId, String ssn)
			throws RegisterException {
		UserEntity user = externalUserDao.findByExternalId(externalId);
		if (user == null) {
			throw new RegisterException("no such user");
		}
		ServiceEntity service = serviceDao.findByShortName(ssn);
		if (service == null) {
			throw new RegisterException("no such service");
		}
		List<RegistryEntity> registryList = dao.findByServiceAndIdentityAndNotStatus(service, user.getIdentity(), 
				RegistryStatus.DELETED, RegistryStatus.DEPROVISIONED);
		
		for (RegistryEntity registry : registryList) {
			registerUserService.deregisterUser(registry, "external", "deregister-via-api");
		}
	}
	
	@Override
	protected BaseEntityMapper<RegistryEntity, RegistryEntityDto> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<RegistryEntity> getDao() {
		return dao;
	}

}
