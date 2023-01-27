package edu.kit.scc.webreg.dao.ops;

public interface NotLike<E> extends Predicate<E, String> {

	String getPattern();

	LikeMatchMode getMatchMode();

}
