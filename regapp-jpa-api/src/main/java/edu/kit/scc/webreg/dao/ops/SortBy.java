package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public interface SortBy {

	static SortBy of(String field, SortOrder sortOrder) {
		return new SortByBasedOnString(field, sortOrder);
	}

	static SortBy of(SingularAttribute<?, ?> field, SortOrder sortOrder, NullOrder nullOrder) {
		return new SortByBasedOnAttribute(field, sortOrder, nullOrder);
	}

	static SortBy ascendingBy(String field) {
		return new SortByBasedOnString(field, SortOrder.ASCENDING);
	}

	static SortBy ascendingBy(SingularAttribute<?, ?> field) {
		return new SortByBasedOnAttribute(field, SortOrder.ASCENDING, NullOrder.DB_DEFAULT);
	}

	static SortBy ascendingBy(SingularAttribute<?, ?> field, NullOrder nullOrder) {
		return new SortByBasedOnAttribute(field, SortOrder.ASCENDING, nullOrder);
	}

	static SortBy descendingBy(String field) {
		return new SortByBasedOnString(field, SortOrder.DESCENDING);
	}

	static SortBy descendingBy(SingularAttribute<?, ?> field) {
		return new SortByBasedOnAttribute(field, SortOrder.DESCENDING, NullOrder.DB_DEFAULT);
	}

	static SortBy descendingBy(SingularAttribute<?, ?> field, NullOrder nullOrder) {
		return new SortByBasedOnAttribute(field, SortOrder.DESCENDING, nullOrder);
	}

	SortOrder getSortOrder();

	NullOrder getNullOrder();

	<E, F> Path<F> getFieldPath(Path<E> parent);

}
