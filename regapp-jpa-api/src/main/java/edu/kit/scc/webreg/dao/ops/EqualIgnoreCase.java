package edu.kit.scc.webreg.dao.ops;

public interface EqualIgnoreCase<E> extends Predicate<E, String> {

	String getAssignedValue();

}
