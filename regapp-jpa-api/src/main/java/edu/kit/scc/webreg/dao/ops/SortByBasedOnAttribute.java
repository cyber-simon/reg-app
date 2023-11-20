package edu.kit.scc.webreg.dao.ops;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.metamodel.SingularAttribute;

public class SortByBasedOnAttribute implements SortBy {

	private SortOrder sortOrder;
	private NullOrder nullOrder;
	private SingularAttribute<?, ?> field;

	public SortByBasedOnAttribute(SingularAttribute<?, ?> field, SortOrder sortOrder, NullOrder nullOrder) {
		this.sortOrder = sortOrder;
		this.nullOrder = nullOrder;
		this.field = field;
	}

	@Override
	public SortOrder getSortOrder() {
		return sortOrder;
	}

	@Override
	public NullOrder getNullOrder() {
		return nullOrder;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, F> Path<F> getFieldPath(Path<E> parent) {
		return parent.get((SingularAttribute<E, F>) field);
	}

	public SingularAttribute<?, ?> getField() {
		return field;
	}

}
