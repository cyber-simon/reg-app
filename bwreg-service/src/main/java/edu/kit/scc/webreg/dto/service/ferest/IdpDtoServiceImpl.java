package edu.kit.scc.webreg.dto.service.ferest;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.SamlIdpMetadataDao;
import edu.kit.scc.webreg.dto.entity.ferest.IdpEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ferest.IdpEntityMapper;
import edu.kit.scc.webreg.dto.service.BaseDtoServiceImpl;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;

@Stateless
public class IdpDtoServiceImpl extends BaseDtoServiceImpl<SamlIdpMetadataEntity, IdpEntityDto> implements IdpDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private IdpEntityMapper mapper;
	
	@Inject
	private SamlIdpMetadataDao dao;
	
	@Override
	protected BaseEntityMapper<SamlIdpMetadataEntity, IdpEntityDto> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<SamlIdpMetadataEntity> getDao() {
		return dao;
	}

}
