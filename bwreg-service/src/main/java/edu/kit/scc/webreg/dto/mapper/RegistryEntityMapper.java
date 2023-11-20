package edu.kit.scc.webreg.dto.mapper;

import java.util.HashSet;

import jakarta.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.entity.RegistryEntity;

@ApplicationScoped
public class RegistryEntityMapper extends AbstractBaseEntityMapper<RegistryEntity, RegistryEntityDto> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(RegistryEntity fromBaseEntity,
			RegistryEntityDto toDtoEntity) {

		toDtoEntity.setUserId(fromBaseEntity.getUser().getId());
		toDtoEntity.setUserUidNumber(fromBaseEntity.getUser().getUidNumber());
		toDtoEntity.setUserEppn(fromBaseEntity.getUser().getEppn());
		toDtoEntity.setUserEmailAddress(fromBaseEntity.getUser().getEmail());
		toDtoEntity.setUserEmailAddresses(new HashSet<String>());
		toDtoEntity.getUserEmailAddresses().add(fromBaseEntity.getUser().getEmail());
		toDtoEntity.getUserEmailAddresses().addAll(fromBaseEntity.getUser().getEmailAddresses());
		toDtoEntity.setServiceShortName(fromBaseEntity.getService().getShortName());
	}

	@Override
	public Class<RegistryEntityDto> getEntityDtoClass() {
		return RegistryEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"registryStatus", "agreedTime", "lastAccessCheck", "lastFullReconcile",
				"lastReconcile", "lastStatusChange", "registryValues"};
	}

}
