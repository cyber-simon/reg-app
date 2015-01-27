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

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import edu.kit.scc.webreg.dao.GenericSortOrder;
import edu.kit.scc.webreg.entity.BaseEntity;
import edu.kit.scc.webreg.service.BaseService;

public class GenericLazyDataModelImpl<E extends BaseEntity<PK>, T extends BaseService<E, PK>, PK extends Serializable>
		extends LazyDataModel<E>
		implements GenericLazyDataModel<E, T, PK> {

	private static final long serialVersionUID = 1L;

	private T service;

	private Map<String, Object> additionalFilterMap;
	
	public GenericLazyDataModelImpl(T service) {
		super();
		this.service = service;
	}

	public GenericLazyDataModelImpl(T service, Map<String, Object> additionalFilterMap) {
		this(service);
		this.additionalFilterMap = additionalFilterMap;
	}
	
	@Override
	public List<E> load(int first, int pageSize, String sortField, 
			SortOrder sortOrder, Map<String, Object> filterMap) {
		
		if (filterMap != null && additionalFilterMap != null) {
			filterMap.putAll(additionalFilterMap);
		}
		
		List<E> returnList;
		
		if (sortOrder == SortOrder.ASCENDING)
			returnList = getService().findAllPaging(first, pageSize, sortField, GenericSortOrder.ASC, filterMap);
		else if (sortOrder == SortOrder.DESCENDING)
			returnList = getService().findAllPaging(first, pageSize, sortField, GenericSortOrder.DESC, filterMap);
		else
			returnList = getService().findAllPaging(first, pageSize, sortField, GenericSortOrder.NONE, filterMap);
		
		setPageSize(pageSize);
		
		Number n = getService().countAll(filterMap);
		if (n != null)
			setRowCount(n.intValue());
		
		return returnList;		
	}
	
	public T getService() {
		return service;
	}
}
