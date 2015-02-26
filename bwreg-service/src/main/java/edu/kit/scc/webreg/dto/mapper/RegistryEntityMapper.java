package edu.kit.scc.webreg.dto.mapper;

import javax.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.entity.RegistryEntity;

@ApplicationScoped
public class RegistryEntityMapper extends AbstractBaseEntityMapper<RegistryEntity, RegistryEntityDto, Long> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(RegistryEntity fromBaseEntity,
			RegistryEntityDto toDtoEntity) {
		toDtoEntity.setRegistryStatus(fromBaseEntity.getRegistryStatus());
	}

	@Override
	public Class<RegistryEntityDto> getEntityDtoClass() {
		return RegistryEntityDto.class;
	}

}
