package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class LessThanOrEqualToBasedOnAttribute<E, F extends Comparable<? super F>> implements LessThanOrEqualTo<E, F> {

	private final SingularAttribute<E, F> field;
	private final F assignedValue;

	protected LessThanOrEqualToBasedOnAttribute(SingularAttribute<E, F> field, F assignedValue) {
		this.field = field;
		this.assignedValue = assignedValue;
	}

	@Override
	public F getAssignedValue() {
		return assignedValue;
	}

	@Override
	public Path<F> getFieldPath(Path<E> parent) {
		return parent.get(field);
	}

}
