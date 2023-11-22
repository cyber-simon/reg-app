package edu.kit.scc.webreg.dto.mapper.ferest;

import jakarta.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.ferest.IdpEntityDto;
import edu.kit.scc.webreg.dto.mapper.AbstractBaseEntityMapper;
import edu.kit.scc.webreg.entity.SamlIdpMetadataEntity;

@ApplicationScoped
public class IdpEntityMapper extends AbstractBaseEntityMapper<SamlIdpMetadataEntity, IdpEntityDto> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(SamlIdpMetadataEntity fromBaseEntity,
			IdpEntityDto toDtoEntity) {

	}

	@Override
	public Class<IdpEntityDto> getEntityDtoClass() {
		return IdpEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] {"entityId", "orgName", "displayName", "description",
				"informationUrl" };
	}

}
