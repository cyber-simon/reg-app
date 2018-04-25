package edu.kit.scc.webreg.dto.mapper;

import java.util.HashMap;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.dto.entity.GroupEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.entity.UserGroupEntity;

@ApplicationScoped
public class ExternalUserEntityMapper extends AbstractBaseEntityMapper<ExternalUserEntity, ExternalUserEntityDto, Long> {

	private static final long serialVersionUID = 1L;

	@Inject
	GroupEntityMapper groupEntityMapper;
	
	@Override
	protected void copyAllProperties(ExternalUserEntity fromBaseEntity,
			ExternalUserEntityDto toDtoEntity) {

		toDtoEntity.setEmailAddresses(new HashSet<String>());
		toDtoEntity.getEmailAddresses().addAll(fromBaseEntity.getEmailAddresses());
		toDtoEntity.setAttributeStore(new HashMap<String, String>());
		toDtoEntity.getAttributeStore().putAll(fromBaseEntity.getAttributeStore());
		toDtoEntity.setGenericStore(new HashMap<String, String>());
		toDtoEntity.getGenericStore().putAll(fromBaseEntity.getGenericStore());
		toDtoEntity.setSecondaryGroups(new HashSet<GroupEntityDto>());
		if (fromBaseEntity.getGroups() != null) {
			for (UserGroupEntity group : fromBaseEntity.getGroups()) {
				GroupEntityDto groupEntityDto = new GroupEntityDto();
				groupEntityMapper.copyProperties(group.getGroup(), groupEntityDto);
				toDtoEntity.getSecondaryGroups().add(groupEntityDto);
			}
		}
		if (fromBaseEntity.getPrimaryGroup() != null) {
			GroupEntityDto groupEntityDto = new GroupEntityDto();
			groupEntityMapper.copyProperties(fromBaseEntity.getPrimaryGroup(), groupEntityDto);
			toDtoEntity.setPrimaryGroup(groupEntityDto);
		}
	}

	@Override
	public Class<ExternalUserEntityDto> getEntityDtoClass() {
		return ExternalUserEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"externalId", "eppn", "email", 
				"givenName", "surName", "uidNumber"};
	}

}
