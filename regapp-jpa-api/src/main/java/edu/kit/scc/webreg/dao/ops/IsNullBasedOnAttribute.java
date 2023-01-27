package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class IsNullBasedOnAttribute<E, F> implements IsNull<E, F> {

	private final SingularAttribute<E, F> field;

	protected IsNullBasedOnAttribute(SingularAttribute<E, F> field) {
		this.field = field;
	}

	@Override
	public Path<F> getFieldPath(Path<E> parent) {
		return parent.get(field);
	}

}
