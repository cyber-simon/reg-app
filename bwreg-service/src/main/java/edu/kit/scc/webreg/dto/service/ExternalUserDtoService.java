package edu.kit.scc.webreg.dto.service;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface ExternalUserDtoService extends BaseDtoService<ExternalUserEntity, ExternalUserEntityDto, Long> {

	ExternalUserEntityDto findByExternalId(String externalId) throws RestInterfaceException ;

	void createExternalUser(ExternalUserEntityDto dto) throws RestInterfaceException ;

	void updateExternalUser(ExternalUserEntityDto dto) throws RestInterfaceException ;

}
