package edu.kit.scc.webreg.dao.ops;

import jakarta.persistence.criteria.Path;

public interface Predicate<E, F> extends RqlExpression {

	Path<F> getFieldPath(Path<E> parent);

}
