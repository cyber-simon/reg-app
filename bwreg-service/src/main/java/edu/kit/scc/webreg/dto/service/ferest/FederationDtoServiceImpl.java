package edu.kit.scc.webreg.dto.service.ferest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.FederationDao;
import edu.kit.scc.webreg.dto.entity.ferest.FederationEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ferest.FederationEntityMapper;
import edu.kit.scc.webreg.dto.service.BaseDtoServiceImpl;
import edu.kit.scc.webreg.entity.FederationEntity;

@Stateless
public class FederationDtoServiceImpl extends BaseDtoServiceImpl<FederationEntity, FederationEntityDto> implements FederationDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private FederationEntityMapper mapper;
	
	@Inject
	private FederationDao dao;
	
	@Override
	protected BaseEntityMapper<FederationEntity, FederationEntityDto> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<FederationEntity> getDao() {
		return dao;
	}

}
