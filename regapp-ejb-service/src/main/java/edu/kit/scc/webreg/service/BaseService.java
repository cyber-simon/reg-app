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

import jakarta.persistence.metamodel.Attribute;

import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.entity.BaseEntity;

public interface BaseService<T extends BaseEntity> extends Serializable {

	T createNew();

	T merge(T entity);

	T save(T entity);

	void delete(T entity);

	List<T> findAll();

	T fetch(Long id);

	@SuppressWarnings("rawtypes")
	T findByIdWithAttrs(Long id, Attribute... attrs);

	List<T> findAll(PaginateBy paginateBy, SortBy sortBy, RqlExpression rqlExpression);

	@SuppressWarnings("rawtypes")
	List<T> findAllEagerly(PaginateBy paginateBy, List<SortBy> sortBy, RqlExpression rqlExpression,
			Attribute... joinFetchBy);

	Number countAll(RqlExpression filterBy);

	List<T> fetchAll(List<Long> ids);

	T findByAttr(String attr, Object value);

	List<T> findAllByAttr(String attr, Object value);

}
