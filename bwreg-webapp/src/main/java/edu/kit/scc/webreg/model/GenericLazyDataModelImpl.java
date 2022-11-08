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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.MatchMode;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import edu.kit.scc.webreg.dao.ops.DaoFilterData;
import edu.kit.scc.webreg.dao.ops.DaoMatchMode;
import edu.kit.scc.webreg.dao.ops.DaoSortData;
import edu.kit.scc.webreg.dao.ops.DaoSortOrder;
import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public class GenericLazyDataModelImpl<E extends BaseEntity, T extends BaseService<E>>
		extends LazyDataModel<E>
		implements GenericLazyDataModel<E, T>, SelectableDataModel<E> {

	private static final long serialVersionUID = 1L;

	private T service;

	private Map<String, Object> filterMap;
	private String[] attrs;
	
	public GenericLazyDataModelImpl(T service) {
		super();
		this.service = service;
	}

	public GenericLazyDataModelImpl(T service, Map<String, Object> filterMap) {
		this(service);
		this.filterMap = filterMap;
	}

	public GenericLazyDataModelImpl(T service, Map<String, Object> filterMap, String... attrs) {
		this(service);
		this.filterMap = filterMap;
		this.attrs = attrs;
	}

	public GenericLazyDataModelImpl(T service, String... attrs) {
		this(service);
		this.attrs = attrs;
	}

	@Override
	public List<E> load(int first, int pageSize,
			Map<String, SortMeta> sortBy, Map<String, FilterMeta> additionalFilterMap) {
		
		List<E> returnList;
		
		Map<String, DaoSortData> sortMap = new HashMap<String, DaoSortData>();
		sortBy.forEach((k, v) -> {  
			DaoSortData dsd = new DaoSortData();
			dsd.setField(v.getField());
			DaoSortOrder dso;
			if (v.getOrder().equals(SortOrder.ASCENDING))
				dso = DaoSortOrder.ASCENDING;
			else if (v.getOrder().equals(SortOrder.DESCENDING))
				dso = DaoSortOrder.DESCENDING;
			else
				dso = DaoSortOrder.UNSORTED;
			dsd.setOrder(dso);
			sortMap.put(k, dsd);
		});
		
		Map<String, DaoFilterData> additionalFilterMapDao = new HashMap<String, DaoFilterData>();
		additionalFilterMap.forEach((k, v) -> {
			DaoFilterData dfd = new DaoFilterData();
			dfd.setFilterValue(v.getFilterValue());
			if (v.getMatchMode().equals(MatchMode.EQUALS)) {
				dfd.setMatchMode(DaoMatchMode.EQUALS);
			}
			else if (v.getMatchMode().equals(MatchMode.STARTS_WITH)) {
				dfd.setMatchMode(DaoMatchMode.STARTS_WITH);
			}
			else if (v.getMatchMode().equals(MatchMode.ENDS_WITH)) {
				dfd.setMatchMode(DaoMatchMode.ENDS_WITH);
			}
			else {
				dfd.setMatchMode(DaoMatchMode.CONTAINS);
			}
			additionalFilterMapDao.put(k, dfd);
		});
		
		returnList = getService().findAllPaging(first, pageSize, sortMap, filterMap, additionalFilterMapDao, attrs);
		
		setPageSize(pageSize);
		
		Number n = getService().countAll(filterMap, additionalFilterMapDao);
		if (n != null)
			setRowCount(n.intValue());
		
		return returnList;
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
