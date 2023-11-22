package edu.kit.scc.webreg.dao.ops;

import jakarta.persistence.criteria.Path;

public class GreaterThanBasedOnString<E, F extends Comparable<? super F>> implements GreaterThan<E, F> {

	private final String field;
	private final F assignedValue;

	protected GreaterThanBasedOnString(String field, F assignedValue) {
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
