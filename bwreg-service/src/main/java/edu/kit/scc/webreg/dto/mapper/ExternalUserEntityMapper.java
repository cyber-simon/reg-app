package edu.kit.scc.webreg.dto.mapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;

@ApplicationScoped
public class ExternalUserEntityMapper extends AbstractBaseEntityMapper<ExternalUserEntity, ExternalUserEntityDto> {

	private static final long serialVersionUID = 1L;

	@Inject 
	private UserEntityMapper userEntityMapper;
	
	@Override
	protected void copyAllProperties(ExternalUserEntity fromBaseEntity,
			ExternalUserEntityDto toDtoEntity) {

		userEntityMapper.copyProperties(fromBaseEntity, toDtoEntity);
	}

	@Override
	public Class<ExternalUserEntityDto> getEntityDtoClass() {
		return ExternalUserEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] { "externalId" };
	}

}
