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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseDao<T extends BaseEntity<PK>, PK extends Serializable> {

    T createNew();
    
    T persist(T entity);

    T merge(T entity);

    List<T> findAll();

    List<T> findAllPaging(int first, int pageSize, String sortField, GenericSortOrder sortOrder,
    		Map<String, Object> filterMap);
    
	Number countAll(Map<String, Object> filterMap);

	T findById(PK id);

    void delete(T entity);

	boolean isPersisted(T entity);

	T findByIdWithAttrs(PK id, String... attrs);

	void refresh(T entity);

}
