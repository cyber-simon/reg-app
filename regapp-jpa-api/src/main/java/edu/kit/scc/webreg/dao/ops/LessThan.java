package edu.kit.scc.webreg.dao.ops;

public interface LessThan<E, F extends Comparable<? super F>> extends Predicate<E, F> {

	F getAssignedValue();

}
