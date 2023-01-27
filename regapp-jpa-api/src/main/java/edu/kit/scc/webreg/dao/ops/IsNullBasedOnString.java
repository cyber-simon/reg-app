package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;

public class IsNullBasedOnString<E, F> implements IsNull<E, F> {

	private final String field;

	protected IsNullBasedOnString(String field) {
		this.field = field;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Path<F> getFieldPath(Path<E> parent) {
		Path<?> path = parent;
		for (String fieldName : field.split("\\.")) {
			path = path.get(fieldName);
		}
		return (Path<F>) path;
	}

}
