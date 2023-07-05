package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class EqualIgnoreCaseBasedOnAttribute<E> implements EqualIgnoreCase<E> {

	private final SingularAttribute<E, String> field;
	private final String assignedValue;

	protected EqualIgnoreCaseBasedOnAttribute(SingularAttribute<E, String> field, String assignedValue) {
		this.field = field;
		this.assignedValue = assignedValue;
	}

	@Override
	public String getAssignedValue() {
		return assignedValue;
	}

	@Override
	public Path<String> getFieldPath(Path<E> parent) {
		return parent.get(field);
	}

}
