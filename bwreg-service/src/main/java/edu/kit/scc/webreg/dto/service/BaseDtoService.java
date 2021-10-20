package edu.kit.scc.webreg.dto.service;

import java.io.Serializable;
import java.util.List;

import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseDtoService<T extends BaseEntity, E extends BaseEntityDto> extends Serializable {

	E createNewDto();

	E findById(Long pk);

	List<E> findAll();
}
