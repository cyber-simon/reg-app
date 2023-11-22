package edu.kit.scc.webreg.dao.ops;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.SingularAttribute;

public class IsNotNullBasedOnAttribute<E, F> implements IsNotNull<E, F> {

	private final SingularAttribute<E, F> field;

	protected IsNotNullBasedOnAttribute(SingularAttribute<E, F> field) {
		this.field = field;
	}

	@Override
	public Path<F> getFieldPath(Path<E> parent) {
		return parent.get(field);
	}

}
