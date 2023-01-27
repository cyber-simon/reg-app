package edu.kit.scc.webreg.dao.ops;

import javax.persistence.criteria.Path;

public class LikeBasedOnString<E> implements Like<E> {

	private final String field;
	private final String pattern;
	private final LikeMatchMode matchMode;

	protected LikeBasedOnString(String field, String pattern, LikeMatchMode matchMode) {
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
