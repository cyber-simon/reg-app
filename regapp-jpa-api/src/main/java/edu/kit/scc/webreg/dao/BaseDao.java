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

import javax.persistence.LockModeType;
import javax.persistence.metamodel.Attribute;

import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseDao<T extends BaseEntity> {

	T createNew();

	T persist(T entity);

	T merge(T entity);

	void delete(T entity);

	boolean isPersisted(T entity);

	void refresh(T entity);

	List<T> findAll();

	List<T> findAll(RqlExpression filterBy);

	List<T> findAll(PaginateBy paginateBy);

	List<T> findAll(PaginateBy paginateBy, RqlExpression filterBy);

	List<T> findAll(PaginateBy paginateBy, SortBy sortBy, RqlExpression filterBy);

	List<T> findAll(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression filterBy);

	@SuppressWarnings("rawtypes")
	List<T> findAllEagerly(RqlExpression filterBy, Attribute... joinFetchBy);

	@SuppressWarnings("rawtypes")
	List<T> findAllEagerly(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression filterBy,
			Attribute... joinFetchBy);

	List<T> fetchAll(List<Long> ids);

	Number countAll(RqlExpression filterBy);

	T fetch(Long id);

	@SuppressWarnings("rawtypes")
	T find(RqlExpression findBy, Attribute... attrs);

	T fetch(Long id, LockModeType lockMode);

}
