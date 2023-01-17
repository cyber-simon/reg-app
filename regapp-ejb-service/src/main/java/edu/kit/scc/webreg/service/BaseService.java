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
package edu.kit.scc.webreg.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.dao.ops.DaoFilterData;
import edu.kit.scc.webreg.dao.ops.DaoSortData;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseService<T extends BaseEntity> extends Serializable {

	T createNew();

	T merge(T entity);

	T save(T entity);

	void delete(T entity);

	List<T> findAll();

	T findById(Long id);

	T findByIdWithAttrs(Long id, String... attrs);

	List<T> findAllPaging(int offset, int limit, Map<String, DaoSortData> sortBy, Map<String, Object> filterMap,
			Map<String, DaoFilterData> additionalFilterMap, String... attrs);

	Number countAll(Map<String, Object> filterMap, Map<String, DaoFilterData> additionalFilterMap);

	List<T> findByMultipleId(List<Long> ids);

	T findByAttr(String attr, Object value);

	List<T> findAllByAttr(String attr, Object value);
}
