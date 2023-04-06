package edu.kit.scc.webreg.dao.ops;

import static edu.kit.scc.webreg.dao.ops.NullOrder.DB_DEFAULT;

import javax.persistence.criteria.Path;

public class SortByBasedOnString implements SortBy {

	private SortOrder sortOrder;
	private String field;

	public SortByBasedOnString(String field, SortOrder sortOrder) {
		this.sortOrder = sortOrder;
		this.field = field;
	}

	@Override
	public SortOrder getSortOrder() {
		return sortOrder;
	}

	@Override
	public NullOrder getNullOrder() {
		return DB_DEFAULT;
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
