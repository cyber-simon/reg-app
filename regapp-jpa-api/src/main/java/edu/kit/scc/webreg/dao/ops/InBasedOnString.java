package edu.kit.scc.webreg.dao.ops;

import java.util.List;

import javax.persistence.criteria.Path;

public class InBasedOnString<E, F> implements In<E, F> {

	private final String field;
	private final List<F> assignedValues;

	protected InBasedOnString(String field, List<F> assignedValues) {
		this.field = field;
		this.assignedValues = assignedValues;
	}

	@Override
	public List<F> getAssignedValues() {
		return assignedValues;
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
