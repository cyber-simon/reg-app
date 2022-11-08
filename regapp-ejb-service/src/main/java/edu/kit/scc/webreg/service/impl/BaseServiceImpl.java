/*******************************************************************************
 * Copyright (c) 2014 Michael Simon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Simon - initial
 ******************************************************************************/
package edu.kit.scc.webreg.service.impl;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GenericSortOrder;
import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public abstract class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {

	private static final long serialVersionUID = 1L;

	protected abstract BaseDao<T> getDao();
	
	@Override
	public T createNew() {
		return getDao().createNew();
	}

	@Override
	public T save(T entity) {
		return getDao().persist(entity);
	}

	@Override
	public T merge(T entity) {
		return getDao().merge(entity);
	}

	@Override
	public void delete(T entity) {
		getDao().delete(entity);
	}

	@Override
	public List<T> findAll() {
		return getDao().findAll();
	}

	@Override
	public List<T> findAllPaging(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, Object> filterMap, Map<String, FilterMeta> additionalFilterMap, String... attrs) {
		return getDao().findAllPaging(first, pageSize, sortBy, filterMap, additionalFilterMap, attrs);
	}

	@Override
	public List<T> findAllPaging(int first, int pageSize, String sortField,
			GenericSortOrder sortOrder, Map<String, Object> filterMap, Map<String, FilterMeta> additionalFilterMap, String... attrs) {
		return getDao().findAllPaging(first, pageSize, sortField, sortOrder, filterMap, additionalFilterMap, attrs);
	}
	
	@Override
	public Number countAll(Map<String, Object> filterMap, Map<String, FilterMeta> additionalFilterMap) {
		return getDao().countAll(filterMap, additionalFilterMap);
	}
	
	@Override
	public T findById(Long id) {
		return getDao().findById(id);
	}

	@Override
	public T findByAttr(String attr, Object value) {
		return getDao().findByAttr(attr, value);
	}

	@Override
	public List<T> findAllByAttr(String attr, Object value) {
		return getDao().findAllByAttr(attr, value);
	}

	@Override
	public List<T> findByMultipleId(List<Long> ids) {
		return getDao().findByMultipleId(ids);
	}

	@Override
	public T findByIdWithAttrs(Long id, String...attrs) {
		return getDao().findByIdWithAttrs(id, attrs);
	}
}
