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
package edu.kit.scc.webreg.dao;

import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.SortMeta;

import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseDao<T extends BaseEntity> {

    T createNew();
    
    T persist(T entity);

    T merge(T entity);

    List<T> findAll();

    List<T> findAllPaging(int first, int pageSize, String sortField, GenericSortOrder sortOrder,
    		Map<String, Object> filterMap, Map<String, FilterMeta> additionalFilterMap, String... attrs);

	List<T> findAllPaging(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, Object> filterMap, Map<String, FilterMeta> additionalFilterMap, String... attrs);
    
	Number countAll(Map<String, Object> filterMap, Map<String, FilterMeta> additionalFilterMap);

	T findById(Long id);

    void delete(T entity);

	boolean isPersisted(T entity);

	T findByIdWithAttrs(Long id, String... attrs);

	void refresh(T entity);

	List<T> findByMultipleId(List<Long> ids);

	T findByAttr(String attr, Object value);

	List<T> findAllByAttr(String attr, Object value);
}
