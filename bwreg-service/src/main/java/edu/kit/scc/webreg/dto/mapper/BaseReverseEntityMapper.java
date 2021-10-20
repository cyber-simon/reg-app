package edu.kit.scc.webreg.dto.mapper;

import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.exc.RestInterfaceException;

public interface BaseReverseEntityMapper<E extends BaseEntityDto, T extends BaseEntity> {

	public void copyProperties(E fromBaseEntity, T toDtoEntity) throws RestInterfaceException;
}
