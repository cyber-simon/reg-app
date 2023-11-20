package edu.kit.scc.webreg.dao.ops;

import jakarta.persistence.criteria.Path;

public class EqualIgnoreCaseBasedOnString<E> implements EqualIgnoreCase<E> {

	private final String field;
	private final String assignedValue;

	protected EqualIgnoreCaseBasedOnString(String field, String assignedValue) {
		this.field = field;
		this.assignedValue = assignedValue;
	}

	@Override
	public String getAssignedValue() {
		return assignedValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Path<String> getFieldPath(Path<E> parent) {
		Path<?> path = parent;
		for (String fieldName : field.split("\\.")) {
			path = path.get(fieldName);
		}
		return (Path<String>) path;
	}

}
