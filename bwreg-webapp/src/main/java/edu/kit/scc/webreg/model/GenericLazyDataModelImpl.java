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
package edu.kit.scc.webreg.model;

import static edu.kit.scc.webreg.dao.ops.RqlExpressions.and;
import static edu.kit.scc.webreg.dao.ops.RqlExpressions.like;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.MatchMode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;

import edu.kit.scc.webreg.dao.ops.LikeMatchMode;
import edu.kit.scc.webreg.dao.ops.PaginateBy;
import edu.kit.scc.webreg.dao.ops.RqlExpression;
import edu.kit.scc.webreg.dao.ops.SortBy;
import edu.kit.scc.webreg.dao.ops.SortOrder;
import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public class GenericLazyDataModelImpl<E extends BaseEntity, T extends BaseService<E>> extends LazyDataModel<E>
		implements GenericLazyDataModel<E, T>, SelectableDataModel<E> {

	private static final long serialVersionUID = 1L;

	private T service;

	private RqlExpression filter;
	private String[] attrs;

	public GenericLazyDataModelImpl(T service) {
		super();
		this.service = service;
	}

	public GenericLazyDataModelImpl(T service, RqlExpression filter) {
		this(service);
		this.filter = filter;
	}

	public GenericLazyDataModelImpl(T service, String... attrs) {
		this(service);
		this.attrs = attrs;
	}

	@Override
	public List<E> load(int first, int pageSize, Map<String, SortMeta> sortBy,
			Map<String, FilterMeta> additionalFilterMap) {

		RqlExpression[] additionalFilters = additionalFilterMap.entrySet().stream()
				.map(e -> like(e.getKey(), e.getValue().getFilterValue().toString(),
						getMatchMode(e.getValue().getMatchMode())))
				.collect(Collectors.toList()).toArray(RqlExpression[]::new);
		RqlExpression completeFilter = and(filter, additionalFilters);

		setPageSize(pageSize);
		Number n = getService().countAll(completeFilter);
		if (n != null) {
			setRowCount(n.intValue());
		}

		List<SortBy> sortList = sortBy.values().stream().map(this::getDaoSortData).collect(Collectors.toList());

		return getService().findAllPaging(PaginateBy.of(first, pageSize), sortList, completeFilter, attrs);
	}

	private SortBy getDaoSortData(SortMeta primefacesSortMeta) {
		return SortBy.of(primefacesSortMeta.getField(), SortOrder.valueOf(primefacesSortMeta.getOrder().name()));
	}

	private LikeMatchMode getMatchMode(MatchMode primefacesMatchMode) {
		switch (primefacesMatchMode) {
		case EQUALS:
			return LikeMatchMode.EQUALS;
		case STARTS_WITH:
			return LikeMatchMode.STARTS_WITH;
		case ENDS_WITH:
			return LikeMatchMode.ENDS_WITH;
		case CONTAINS:
			return LikeMatchMode.CONTAINS;
		default:
			throw new UnsupportedOperationException(
					String.format("Match mode '%' is not supported", primefacesMatchMode));
		}
	}

	public T getService() {
		return service;
	}

	@Override
	public E getRowData(String rowKey) {
		Long id = Long.parseLong(rowKey);
		if (id instanceof Serializable) {
			return getService().findById(id);
		}
		return null;
	}

	@Override
	public String getRowKey(E object) {
		return object.getId().toString();
	}
}
