package edu.kit.scc.webreg.dto.service;

import java.io.Serializable;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.entity.BaseEntity;

public abstract class BaseDtoServiceImpl<T extends BaseEntity<PK>, E extends BaseEntityDto<PK>, PK extends Serializable> 
	implements BaseDtoService<T, E, PK> {

	private static final long serialVersionUID = 1L;

	protected abstract BaseEntityMapper<T, E, PK> getMapper();
	protected abstract BaseDao<T, PK> getDao();

	@Override
	public E createNewDto() {
		try {
			return getMapper().getEntityDtoClass().newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}
	}

	@Override
	public E findById(PK pk) {
		E dto = createNewDto();
		getMapper().copyProperties(getDao().findById(pk), dto);
		return dto;
	}
}
