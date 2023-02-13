package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;

public class LessThanBasedOnString<E, F extends Comparable<? super F>> implements LessThan<E, F> {

	private final String field;
	private final F assignedValue;

	protected LessThanBasedOnString(String field, F assignedValue) {
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
