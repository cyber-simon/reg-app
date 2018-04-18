package edu.kit.scc.webreg.dto.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ExternalUserDao;
import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ExternalUserEntityMapper;
import edu.kit.scc.webreg.entity.ExternalUserEntity;

@Stateless
public class ExternalUserDtoServiceImpl extends BaseDtoServiceImpl<ExternalUserEntity, ExternalUserEntityDto, Long> implements ExternalUserDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private ExternalUserEntityMapper mapper;
	
	@Inject
	private ExternalUserDao dao;

	@Override
	public ExternalUserEntityDto findByExternalId(String externalId) {
		ExternalUserEntity entity = dao.findByExternalId(externalId);
		ExternalUserEntityDto dto = createNewDto();
		mapper.copyProperties(entity, dto);
		return dto;
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
