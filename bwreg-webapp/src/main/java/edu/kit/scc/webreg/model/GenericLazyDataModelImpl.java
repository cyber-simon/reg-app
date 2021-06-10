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
import java.util.List;
import java.util.Map;

import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;

import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public class GenericLazyDataModelImpl<E extends BaseEntity<PK>, T extends BaseService<E, PK>, PK extends Serializable>
		extends LazyDataModel<E>
		implements GenericLazyDataModel<E, T, PK>, SelectableDataModel<E> {

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
		
		returnList = getService().findAllPaging(first, pageSize, sortBy, filterMap, additionalFilterMap, attrs);
		
		setPageSize(pageSize);
		
		Number n = getService().countAll(filterMap, additionalFilterMap);
		if (n != null)
			setRowCount(n.intValue());
		
		return returnList;
	}
	
	public T getService() {
		return service;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E getRowData(String rowKey) {
		Long id = Long.parseLong(rowKey);
		if (id instanceof Serializable) {
			return getService().findById((PK) id);
		}
		return null;
	}

	@Override
	public String getRowKey(E object) {
		return object.getId().toString();
	}
}
