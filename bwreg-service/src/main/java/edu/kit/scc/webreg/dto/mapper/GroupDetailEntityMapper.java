package edu.kit.scc.webreg.dto.mapper;

import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.dto.entity.UserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.entity.UserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;

@ApplicationScoped
public class GroupDetailEntityMapper extends AbstractBaseEntityMapper<GroupEntity, GroupEntityDto, Long> {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserEntityMapper userEntityMapper;
	
	@Inject
	private ExternalUserEntityMapper externalUserEntityMapper;
	
	@Override
	protected void copyAllProperties(GroupEntity fromBaseEntity,
			GroupEntityDto toDtoEntity) {

		toDtoEntity.setParents(new HashSet<GroupEntityDto>());
		for (GroupEntity parent : fromBaseEntity.getParents()) {
			GroupEntityDto parentDto = new GroupEntityDto();
			copyProperties(parent, parentDto);
			toDtoEntity.getParents().add(parentDto);
		}
		
		toDtoEntity.setUsers(new HashSet<UserEntityDto>());
		for (UserGroupEntity userGroupEntity : fromBaseEntity.getUsers()) {
			UserEntity user = userGroupEntity.getUser();
			if (user instanceof ExternalUserEntity) {
				ExternalUserEntityDto userDto = new ExternalUserEntityDto();
				externalUserEntityMapper.copyProperties((ExternalUserEntity) user, userDto);
				toDtoEntity.getUsers().add(userDto);
			}
			else {
				UserEntityDto userDto = new UserEntityDto();
				userEntityMapper.copyProperties(user, userDto);
				toDtoEntity.getUsers().add(userDto);
			}
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
