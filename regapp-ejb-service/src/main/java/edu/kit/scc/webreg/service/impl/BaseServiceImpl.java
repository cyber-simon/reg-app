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

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.ops.DaoFilterData;
import edu.kit.scc.webreg.dao.ops.DaoSortData;
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
	public List<T> findAllPaging(int offset, int limit, Map<String, DaoSortData> sortBy,
			Map<String, Object> filterMap, Map<String, DaoFilterData> additionalFilterMap, String... attrs) {
		return getDao().findAllPaging(offset, limit, sortBy, filterMap, additionalFilterMap, attrs);
	}

	@Override
	public Number countAll(Map<String, Object> filterMap, Map<String, DaoFilterData> additionalFilterMap) {
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
	public T findByIdWithAttrs(Long id, String... attrs) {
		return getDao().findByIdWithAttrs(id, attrs);
	}
}
