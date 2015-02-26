package edu.kit.scc.webreg.dto.service;

import java.io.Serializable;

import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseDtoService<T extends BaseEntity<PK>, E extends BaseEntityDto<PK>, PK extends Serializable> extends Serializable {

	E createNewDto();

	E findById(PK pk);
}
