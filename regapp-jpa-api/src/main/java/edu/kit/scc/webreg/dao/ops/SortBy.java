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

	SortOrder getOrder();

	<E, F> Path<F> getFieldPath(Path<E> parent);

}
