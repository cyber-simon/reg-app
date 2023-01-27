package edu.kit.scc.webreg.dao.ops;

public interface Equal<E, F> extends Predicate<E, F> {

	F getAssignedValue();

}
