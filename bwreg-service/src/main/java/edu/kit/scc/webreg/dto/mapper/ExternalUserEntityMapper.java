package edu.kit.scc.webreg.dto.mapper;

import java.util.HashMap;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;

@ApplicationScoped
public class ExternalUserEntityMapper extends AbstractBaseEntityMapper<ExternalUserEntity, ExternalUserEntityDto, Long> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(ExternalUserEntity fromBaseEntity,
			ExternalUserEntityDto toDtoEntity) {

		toDtoEntity.setEmailAddresses(new HashSet<String>());
		toDtoEntity.getEmailAddresses().addAll(fromBaseEntity.getEmailAddresses());
		toDtoEntity.setAttributeStore(new HashMap<String, String>());
		toDtoEntity.getAttributeStore().putAll(fromBaseEntity.getAttributeStore());
		toDtoEntity.setGenericStore(new HashMap<String, String>());
		toDtoEntity.getGenericStore().putAll(fromBaseEntity.getGenericStore());
	}

	@Override
	public Class<ExternalUserEntityDto> getEntityDtoClass() {
		return ExternalUserEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"externalId", "eppn", "emailAddress", 
				"givenName", "surName", "uidNumber"};
	}

}
