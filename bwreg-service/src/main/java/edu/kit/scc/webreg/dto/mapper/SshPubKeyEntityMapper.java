package edu.kit.scc.webreg.dto.mapper;

import javax.enterprise.context.ApplicationScoped;

import edu.kit.scc.webreg.dto.entity.SshPubKeyEntityDto;
import edu.kit.scc.webreg.entity.SshPubKeyEntity;

@ApplicationScoped
public class SshPubKeyEntityMapper extends AbstractBaseEntityMapper<SshPubKeyEntity, SshPubKeyEntityDto> {

	private static final long serialVersionUID = 1L;

	@Override
	protected void copyAllProperties(SshPubKeyEntity fromBaseEntity,
			SshPubKeyEntityDto toDtoEntity) {

	}

	@Override
	public Class<SshPubKeyEntityDto> getEntityDtoClass() {
		return SshPubKeyEntityDto.class;
	}

	@Override
	protected String[] getPropertiesToCopy() {
		return new String[] { "name", "command", 
				"from", "comment", "encodedKey", "keyType", "expiresAt", "keyStatus"};
	}

}
