package edu.kit.scc.webreg.dto.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dto.entity.BaseEntityDto;
import edu.kit.scc.webreg.dto.mapper.BaseEntityMapper;
import edu.kit.scc.webreg.entity.BaseEntity;

public abstract class BaseDtoServiceImpl<T extends BaseEntity, E extends BaseEntityDto> 
	implements BaseDtoService<T, E> {

	private static final long serialVersionUID = 1L;

	protected abstract BaseEntityMapper<T, E> getMapper();
	protected abstract BaseDao<T> getDao();

	@Override
	public E createNewDto() {
		try {
			return getMapper().getEntityDtoClass().getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			return null;
		}
	}

	@Override
	public E findById(Long pk) {
		E dto = createNewDto();
		getMapper().copyProperties(getDao().fetch(pk), dto);
		return dto;
	}
	
	@Override
	public List<E> findAll() {
		List<T> daoList = getDao().findAll();
		List<E> dtoList = new ArrayList<E>(daoList.size());
		for (T dao : daoList) {
			E dto = createNewDto();
			getMapper().copyProperties(dao, dto);
			dtoList.add(dto);
		}
		return dtoList;
	}	
}
