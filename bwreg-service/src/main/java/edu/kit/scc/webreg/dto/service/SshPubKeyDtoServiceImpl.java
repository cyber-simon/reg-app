package edu.kit.scc.webreg.dto.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.SshPubKeyEntityMapper;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.SshPubKeyStatus;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Stateless
public class SshPubKeyDtoServiceImpl extends BaseDtoServiceImpl<SshPubKeyEntity, SshPubKeyEntityDto> implements SshPubKeyDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserDao userDao;
	
	@Inject
	private SshPubKeyDao dao;
	
	@Inject
	private SshPubKeyEntityMapper mapper;

	@Override
	public List<SshPubKeyEntityDto> findByUidNumber(Long uidNumber) throws RestInterfaceException {
		UserEntity user = userDao.findByUidNumber(uidNumber);
		
		List<SshPubKeyEntity> list = dao.findByIdentity(user.getIdentity().getId());
		
		return convertList(list);
	}	
	
	@Override
	public List<SshPubKeyEntityDto> findByUidNumberAndStatus(Long uidNumber, SshPubKeyStatus keyStatus) throws RestInterfaceException {
		UserEntity user = userDao.findByUidNumber(uidNumber);
		
		List<SshPubKeyEntity> list = dao.findByIdentityAndStatus(user.getIdentity().getId(), keyStatus);
		
		return convertList(list);
	}	
	
	@Override
	public List<SshPubKeyEntityDto> findByUidNumberAndExpiryInDays(Long uidNumber, Integer days) throws RestInterfaceException {
		UserEntity user = userDao.findByUidNumber(uidNumber);
		
		List<SshPubKeyEntity> list = dao.findByIdentityAndExpiryInDays(user.getIdentity().getId(), days);
		
		return convertList(list);
	}

	protected List<SshPubKeyEntityDto> convertList(List<SshPubKeyEntity> list) {
		List<SshPubKeyEntityDto> dtoList = new ArrayList<SshPubKeyEntityDto>(list.size());
		
		for (SshPubKeyEntity key : list) {
			SshPubKeyEntityDto dto = createNewDto();
			mapper.copyProperties(key, dto);
			dtoList.add(dto);
		}

		return dtoList;
	}
	
	@Override
	protected BaseEntityMapper<SshPubKeyEntity, SshPubKeyEntityDto> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<SshPubKeyEntity> getDao() {
		return dao;
	}

}
