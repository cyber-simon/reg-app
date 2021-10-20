package edu.kit.scc.webreg.dto.mapper;

import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseEntityMapper<T extends BaseEntity, E extends BaseEntityDto> {

	void copyProperties(T fromBaseEntity, E toDtoEntity);
	Class<E> getEntityDtoClass();
}
