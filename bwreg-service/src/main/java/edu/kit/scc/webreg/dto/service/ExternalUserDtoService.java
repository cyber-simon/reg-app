package edu.kit.scc.webreg.dto.service;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.UserCreateException;

public interface ExternalUserDtoService extends BaseDtoService<ExternalUserEntity, ExternalUserEntityDto, Long> {

	ExternalUserEntityDto findByExternalId(String externalId) throws NoUserFoundException;

	void createExternalUser(ExternalUserEntityDto dto) throws UserCreateException;

}
