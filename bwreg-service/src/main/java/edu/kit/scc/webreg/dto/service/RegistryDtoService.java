package edu.kit.scc.webreg.dto.service;

import java.util.List;

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.entity.RegistryEntity;

public interface RegistryDtoService extends BaseDtoService<RegistryEntity, RegistryEntityDto, Long> {

	List<RegistryEntityDto> findRegistriesForDepro(String serviceShortName);

}
