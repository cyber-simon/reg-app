package edu.kit.scc.webreg.dto.mapper;

import java.io.Serializable;

import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseEntityMapper<T extends BaseEntity<PK>, E extends BaseEntityDto<PK>, PK extends Serializable> {

	void copyProperties(T fromBaseEntity, E toDtoEntity);
	Class<E> getEntityDtoClass();
}
