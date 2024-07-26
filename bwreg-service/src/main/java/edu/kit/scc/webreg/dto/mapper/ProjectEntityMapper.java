package edu.kit.scc.webreg.dto.mapper;

import java.util.HashSet;

import edu.kit.scc.webreg.dto.entity.ProjectEntityDto;
import edu.kit.scc.webreg.entity.project.ProjectEntity;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProjectEntityMapper extends AbstractBaseEntityMapper<ProjectEntity, ProjectEntityDto> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(ProjectEntity fromBaseEntity, ProjectEntityDto toDtoEntity) {

		if (fromBaseEntity.getParentProject() != null)
			toDtoEntity.setParentProjectId(fromBaseEntity.getParentProject().getId());
		if (fromBaseEntity.getChildProjects() != null)
			toDtoEntity.setChildProjects(
					new HashSet<Long>(fromBaseEntity.getChildProjects().stream().map(p -> p.getId()).toList()));
	}

	@Override
	public Class<ProjectEntityDto> getEntityDtoClass() {
		return ProjectEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] { "name", "shortName", "groupName", "description", "shortDescription", "subProjectsAllowed",
				"published", "approved", "attributePrefix", "attributeName", "projectStatus" };
	}

}
