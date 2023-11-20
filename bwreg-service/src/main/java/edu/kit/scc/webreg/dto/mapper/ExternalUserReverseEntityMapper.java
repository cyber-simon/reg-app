package edu.kit.scc.webreg.dto.mapper;

import java.util.HashMap;
import java.util.HashSet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import edu.kit.scc.webreg.dao.GroupDao;
import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.entity.GroupEntity;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;

@ApplicationScoped
public class ExternalUserReverseEntityMapper extends AbstractBaseReverseEntityMapper<ExternalUserEntityDto, ExternalUserEntity> {

	private static final long serialVersionUID = 1L;

	@Inject
	private GroupDao groupDao;
	
	@Override
	protected void copyAllProperties(ExternalUserEntityDto fromDtoEntity, ExternalUserEntity toBaseEntity) throws RestInterfaceException {

		if (fromDtoEntity.getEmailAddresses() != null) {
			toBaseEntity.setEmailAddresses(new HashSet<String>());
			toBaseEntity.getEmailAddresses().addAll(fromDtoEntity.getEmailAddresses());
		}
		
		if (fromDtoEntity.getAttributeStore() != null) {
		toBaseEntity.setAttributeStore(new HashMap<String, String>());
		toBaseEntity.getAttributeStore().putAll(fromDtoEntity.getAttributeStore());
		}
		
		if (fromDtoEntity.getGenericStore() != null) {
			toBaseEntity.setGenericStore(new HashMap<String, String>());
			toBaseEntity.getGenericStore().putAll(fromDtoEntity.getGenericStore());
		}
		
		if (fromDtoEntity.getPrimaryGroup() != null && fromDtoEntity.getPrimaryGroup().getId() != null) {
			GroupEntity group = groupDao.fetch(fromDtoEntity.getPrimaryGroup().getId());
			if (group == null) {
				throw new NoUserFoundException("no such group");
			}
			else {
				toBaseEntity.setPrimaryGroup(group);
			}
		}
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"externalId", "eppn", "email", 
				"givenName", "surName"};
	}


}
