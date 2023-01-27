package edu.kit.scc.webreg.dao.ops;

public interface Like<E> extends Predicate<E, String> {

	String getPattern();

	LikeMatchMode getMatchMode();

}
