package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;

public class SortByBasedOnString implements SortBy {

	private SortOrder order;
	private String field;

	public SortByBasedOnString(String field, SortOrder order) {
		this.order = order;
		this.field = field;
	}

	@Override
	public SortOrder getOrder() {
		return order;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, F> Path<F> getFieldPath(Path<E> parent) {
		Path<?> path = parent;
		for (String fieldName : field.split("\\.")) {
			path = path.get(fieldName);
		}
		return (Path<F>) path;
	}

}
