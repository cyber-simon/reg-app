package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;
import javax.persistence.metamodel.SingularAttribute;

public interface SortBy {

	public static SortBy of(String field, SortOrder sortOrder) {
		return new SortByBasedOnString(field, sortOrder);
	}

	public static SortBy of(SingularAttribute<?, ?> field, SortOrder sortOrder) {
		return new SortByBasedOnAttribute(field, sortOrder);
	}

	public static SortBy ascendingBy(String field) {
		return new SortByBasedOnString(field, SortOrder.ASCENDING);
	}

	public static SortBy ascendingBy(SingularAttribute<?, ?> field) {
		return new SortByBasedOnAttribute(field, SortOrder.ASCENDING);
	}

	public static SortBy descendingBy(String field) {
		return new SortByBasedOnString(field, SortOrder.DESCENDING);
	}

	public static SortBy descendingBy(SingularAttribute<?, ?> field) {
		return new SortByBasedOnAttribute(field, SortOrder.DESCENDING);
	}

	SortOrder getOrder();

	<E, F> Path<F> getFieldPath(Path<E> parent);

}
