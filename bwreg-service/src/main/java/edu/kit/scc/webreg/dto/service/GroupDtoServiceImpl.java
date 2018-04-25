package edu.kit.scc.webreg.dto.service;

import javax.ejb.Stateless;
import javax.inject.Inject;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.GroupEntityMapper;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.exc.NoUserFoundException;

@Stateless
public class GroupDtoServiceImpl extends BaseDtoServiceImpl<GroupEntity, GroupEntityDto, Long> implements GroupDtoService {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupEntityMapper mapper;

	@Inject
	private GroupDao dao;

	@Override
	public GroupEntityDto findByName(String name) throws NoUserFoundException {
		GroupEntity entity = dao.findByName(name);
		if (entity == null)
			throw new NoUserFoundException("no such user");
		GroupEntityDto dto = createNewDto();
		mapper.copyProperties(entity, dto);
		return dto;
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
