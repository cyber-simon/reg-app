package edu.kit.scc.webreg.dao.ops;

public interface NotEqual<E, F> extends Predicate<E, F> {

	F getAssignedValue();

}
