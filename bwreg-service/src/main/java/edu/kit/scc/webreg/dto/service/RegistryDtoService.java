package edu.kit.scc.webreg.dto.service;

import java.util.List;

import edu.kit.scc.webreg.dto.entity.RegistryEntityDto;
import edu.kit.scc.webreg.entity.RegistryEntity;
import edu.kit.scc.webreg.entity.RegistryStatus;
import edu.kit.scc.webreg.entity.ServiceEntity;
import edu.kit.scc.webreg.exc.RegisterException;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface RegistryDtoService extends BaseDtoService<RegistryEntity, RegistryEntityDto> {

	List<RegistryEntityDto> findRegistriesForDepro(String serviceShortName) throws RestInterfaceException;

	List<RegistryEntityDto> findByExternalId(String externalId);

	RegistryEntityDto register(String externalId, String ssn) throws RegisterException;

	void deregister(String externalId, String ssn) throws RegisterException;

	List<RegistryEntityDto> findRegistriesByStatus(ServiceEntity service, RegistryStatus status);

	List<RegistryEntityDto> findRegistriesByAttribute(String key, String value, ServiceEntity service);

	List<RegistryEntityDto> findAllExternalBySsn(String ssn);
}
