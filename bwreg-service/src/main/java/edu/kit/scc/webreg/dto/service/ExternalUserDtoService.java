package edu.kit.scc.webreg.dto.service;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;

public interface ExternalUserDtoService extends BaseDtoService<ExternalUserEntity, ExternalUserEntityDto, Long> {

	ExternalUserEntityDto findByExternalId(String externalId);

}
