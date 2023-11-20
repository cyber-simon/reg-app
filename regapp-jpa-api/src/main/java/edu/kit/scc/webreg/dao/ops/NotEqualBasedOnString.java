package edu.kit.scc.webreg.dao.ops;

import jakarta.persistence.criteria.Path;

public class NotEqualBasedOnString<E, F> implements NotEqual<E, F> {

	private final String field;
	private final F assignedValue;

	protected NotEqualBasedOnString(String field, F assignedValue) {
		this.field = field;
		this.assignedValue = assignedValue;
	}

	@Override
	public F getAssignedValue() {
		return assignedValue;
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
