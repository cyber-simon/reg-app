package edu.kit.scc.webreg.dao.ops;

import jakarta.persistence.criteria.Path;

public class NotLikeBasedOnString<E> implements NotLike<E> {

	private final String field;
	private final String pattern;
	private final LikeMatchMode matchMode;

	protected NotLikeBasedOnString(String field, String pattern, LikeMatchMode matchMode) {
		this.field = field;
		this.pattern = pattern;
		this.matchMode = matchMode;
	}

	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public LikeMatchMode getMatchMode() {
		return matchMode;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Path<String> getFieldPath(Path<E> parent) {
		Path<?> path = parent;
		for (String fieldName : field.split("\\.")) {
			path = path.get(fieldName);
		}
		return (Path<String>) path;
	}

}
