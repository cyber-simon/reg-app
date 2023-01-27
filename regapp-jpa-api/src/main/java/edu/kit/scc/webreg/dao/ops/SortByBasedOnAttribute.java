package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public class SortByBasedOnAttribute implements SortBy {

	private SortOrder order;
	private SingularAttribute<?, ?> field;

	public SortByBasedOnAttribute(SingularAttribute<?, ?> field, SortOrder order) {
		this.order = order;
		this.field = field;
	}

	@Override
	public SortOrder getOrder() {
		return order;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, F> Path<F> getFieldPath(Path<E> parent) {
		return parent.get((SingularAttribute<E, F>) field);
	}

}
