package edu.kit.scc.webreg.dto.service;

import java.util.List;

import edu.kit.scc.webreg.dto.entity.ExternalUserEntityDto;
import edu.kit.scc.webreg.entity.ExternalUserAdminRoleEntity;
import edu.kit.scc.webreg.entity.ExternalUserEntity;
import edu.kit.scc.webreg.exc.NoUserFoundException;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface ExternalUserDtoService extends BaseDtoService<ExternalUserEntity, ExternalUserEntityDto, Long> {

	ExternalUserEntityDto findByExternalId(String externalId) throws RestInterfaceException ;

	void createExternalUser(ExternalUserEntityDto dto, ExternalUserAdminRoleEntity role) throws RestInterfaceException ;

	void updateExternalUser(ExternalUserEntityDto dto, ExternalUserAdminRoleEntity role) throws RestInterfaceException ;

	void activateExternalUser(String externalId, ExternalUserAdminRoleEntity role) throws RestInterfaceException;

	void deactivateExternalUser(String externalId, ExternalUserAdminRoleEntity role) throws RestInterfaceException;

	List<ExternalUserEntityDto> findByAttribute(String key, String value, ExternalUserAdminRoleEntity adminRole)
			throws NoUserFoundException;

	List<ExternalUserEntityDto> findByGeneric(String key, String value, ExternalUserAdminRoleEntity adminRole)
			throws NoUserFoundException;

	List<ExternalUserEntityDto> findAll(ExternalUserAdminRoleEntity adminRole) throws NoUserFoundException;

}
