package edu.kit.scc.webreg.dto.service;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SshPubKeyDao;
import edu.kit.scc.webreg.dao.UserDao;
import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.SshPubKeyEntityMapper;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@Stateless
public class SshPubKeyDtoServiceImpl extends BaseDtoServiceImpl<SshPubKeyEntity, SshPubKeyEntityDto, Long> implements SshPubKeyDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private Logger logger;
	
	@Inject
	private UserDao userDao;
	
	@Inject
	private SshPubKeyDao dao;
	
	@Inject
	private SshPubKeyEntityMapper mapper;

	@Override
	public List<SshPubKeyEntityDto> findByUidNumber(Long uidNumber) throws RestInterfaceException {
		UserEntity user = userDao.findByUidNumber(uidNumber);
		
		List<SshPubKeyEntity> list = dao.findByUser(user.getId());
		List<SshPubKeyEntityDto> dtoList = new ArrayList<SshPubKeyEntityDto>(list.size());
		
		for (SshPubKeyEntity key : list) {
			SshPubKeyEntityDto dto = createNewDto();
			mapper.copyProperties(key, dto);
			dtoList.add(dto);
		}

		return dtoList;
	}	
	
	@Override
	protected BaseEntityMapper<SshPubKeyEntity, SshPubKeyEntityDto, Long> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<SshPubKeyEntity, Long> getDao() {
		return dao;
	}

}
