package edu.kit.scc.webreg.dto.mapper;

import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.entity.GroupEntity;

@ApplicationScoped
public class GroupEntityMapper extends AbstractBaseEntityMapper<GroupEntity, GroupEntityDto, Long> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(GroupEntity fromBaseEntity,
			GroupEntityDto toDtoEntity) {

		toDtoEntity.setParents(new HashSet<GroupEntityDto>());
		for (GroupEntity parent : fromBaseEntity.getParents()) {
			GroupEntityDto parentDto = new GroupEntityDto();
			copyProperties(parent, parentDto);
			toDtoEntity.getParents().add(parentDto);
		}
	}

	@Override
	public Class<GroupEntityDto> getEntityDtoClass() {
		return GroupEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"name", "gidNumber"};
	}

}
