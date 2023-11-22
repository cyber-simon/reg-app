package edu.kit.scc.webreg.dao.ops;

import java.util.List;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.SingularAttribute;

public class InBasedOnAttribute<E, F> implements In<E, F> {

	private final SingularAttribute<E, F> field;
	private final List<F> assignedValues;

	protected InBasedOnAttribute(SingularAttribute<E, F> field, List<F> assignedValues) {
		this.field = field;
		this.assignedValues = assignedValues;
	}

	@Override
	public List<F> getAssignedValues() {
		return assignedValues;
	}

	@Override
	public Path<F> getFieldPath(Path<E> parent) {
		return parent.get(field);
	}

}
