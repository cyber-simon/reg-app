package edu.kit.scc.webreg.dto.mapper;

import java.io.Serializable;

import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseReverseEntityMapper<E extends BaseEntityDto<PK>, T extends BaseEntity<PK>, PK extends Serializable> {

	public void copyProperties(E fromBaseEntity, T toDtoEntity);
}
