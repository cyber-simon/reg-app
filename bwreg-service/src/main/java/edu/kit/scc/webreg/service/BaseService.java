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

import edu.kit.scc.webreg.dao.GenericSortOrder;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseService<T extends BaseEntity<PK>, PK extends Serializable> extends Serializable {

    T createNew();

    T merge(T entity);

    T save(T entity);

    void delete(T entity);

    List<T> findAll();

    T findById(PK id);

    T findByIdWithAttrs(PK id, String... attrs);

	List<T> findAllPaging(int first, int pageSize, String sortField,
			GenericSortOrder sortOrder, Map<String, Object> filterMap);

	Number countAll(Map<String, Object> filterMap);

}
