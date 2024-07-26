package edu.kit.scc.webreg.dto.service;

import java.util.ArrayList;
import java.util.List;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.project.ProjectDao;
import edu.kit.scc.webreg.dto.entity.ProjectEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.dto.mapper.ProjectEntityMapper;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import edu.kit.scc.webreg.entity.project.ProjectServiceEntity;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

@Stateless
public class ProjectDtoService extends BaseDtoServiceImpl<ProjectEntity, ProjectEntityDto> {

	private static final long serialVersionUID = 1L;

	@Inject
	private ProjectEntityMapper mapper;
	
	@Inject
	private ProjectDao dao;
	
	public List<ProjectEntityDto> findByService(ServiceEntity service) {
		List<ProjectServiceEntity> projectList = dao.findAllByService(service);
		List<ProjectEntityDto> dtoList = new ArrayList<ProjectEntityDto>(projectList.size());
		for (ProjectServiceEntity p : projectList) {
			ProjectEntityDto dto = createNewDto();
			mapper.copyProperties(p.getProject(), dto);
			dtoList.add(dto);
		}
		return dtoList;
	}
	
	@Override
	protected BaseEntityMapper<ProjectEntity, ProjectEntityDto> getMapper() {
		return mapper;
	}

	@Override
	protected BaseDao<ProjectEntity> getDao() {
		return dao;
	}

}
