package edu.kit.scc.webreg.dto.mapper.ferest;

import jakarta.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.ferest.FederationEntityDto;
import edu.kit.scc.webreg.dto.mapper.AbstractBaseEntityMapper;
import edu.kit.scc.webreg.entity.FederationEntity;

@ApplicationScoped
public class FederationEntityMapper extends AbstractBaseEntityMapper<FederationEntity, FederationEntityDto> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(FederationEntity fromBaseEntity,
			FederationEntityDto toDtoEntity) {

	}

	@Override
	public Class<FederationEntityDto> getEntityDtoClass() {
		return FederationEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"entityId", "name", "polledAt" };
	}

}
