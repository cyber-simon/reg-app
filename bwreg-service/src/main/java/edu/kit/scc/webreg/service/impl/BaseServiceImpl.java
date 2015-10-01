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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.dao.BaseDao;
import edu.kit.scc.webreg.dao.GenericSortOrder;
import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public abstract class BaseServiceImpl<T extends BaseEntity<PK>, PK extends Serializable> implements BaseService<T, PK> {

	private static final long serialVersionUID = 1L;

	protected abstract BaseDao<T, PK> getDao();
	
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
	public List<T> findAllPaging(int first, int pageSize, String sortField,
			GenericSortOrder sortOrder, Map<String, Object> filterMap) {
		return getDao().findAllPaging(first, pageSize, sortField, sortOrder, filterMap);
	}
	
	@Override
	public Number countAll(Map<String, Object> filterMap) {
		return getDao().countAll(filterMap);
	}
	
	@Override
	public T findById(PK id) {
		return getDao().findById(id);
	}

	@Override
	public T findByIdWithAttrs(PK id, String...attrs) {
		return getDao().findByIdWithAttrs(id, attrs);
	}
}
