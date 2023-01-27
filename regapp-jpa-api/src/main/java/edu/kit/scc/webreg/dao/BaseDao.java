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

import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseDao<T extends BaseEntity> {

	T createNew();

	T persist(T entity);

	T merge(T entity);

	List<T> findAll();

	List<T> findAllPaging(RqlExpression filterBy);

	List<T> findAllPaging(PaginateBy paginateBy);

	List<T> findAllPaging(PaginateBy paginateBy, RqlExpression filterBy);

	List<T> findAllPaging(PaginateBy paginateBy, SortBy sortBy, RqlExpression filterBy);

	List<T> findAllPaging(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression filterBy, String... joinFetchBy);

	Number countAll(RqlExpression filterBy);

	T findById(Long id);

	void delete(T entity);

	boolean isPersisted(T entity);

	T findByIdWithAttrs(Long id, String... attrs);

	void refresh(T entity);

	List<T> findByMultipleId(List<Long> ids);

	T findByAttr(String attr, Object value);

}
