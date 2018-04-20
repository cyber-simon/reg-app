package edu.kit.scc.webreg.dto.mapper;

import java.util.HashMap;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;

@ApplicationScoped
public class ExternalUserReverseEntityMapper extends AbstractBaseReverseEntityMapper<ExternalUserEntityDto, ExternalUserEntity, Long> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(ExternalUserEntityDto fromBaseEntity, ExternalUserEntity toDtoEntity) {

		if (fromBaseEntity.getEmailAddresses() != null) {
			toDtoEntity.setEmailAddresses(new HashSet<String>());
			toDtoEntity.getEmailAddresses().addAll(fromBaseEntity.getEmailAddresses());
		}
		
		if (fromBaseEntity.getAttributeStore() != null) {
		toDtoEntity.setAttributeStore(new HashMap<String, String>());
		toDtoEntity.getAttributeStore().putAll(fromBaseEntity.getAttributeStore());
		}
		
		if (fromBaseEntity.getGenericStore() != null) {
			toDtoEntity.setGenericStore(new HashMap<String, String>());
			toDtoEntity.getGenericStore().putAll(fromBaseEntity.getGenericStore());
		}
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"externalId", "eppn", "email", 
				"givenName", "surName"};
	}


}
